package org.verapdf.parser;

import org.apache.log4j.Logger;
import org.verapdf.as.CharTable;
import org.verapdf.as.io.ASInputStream;
import org.verapdf.cos.filters.COSFilterASCIIHexDecode;
import org.verapdf.io.InternalInputStream;
import org.verapdf.io.SeekableStream;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import static org.verapdf.as.CharTable.*;

/**
 * @author Timur Kamalov
 */
public class BaseParser {

	private static final Logger LOGGER = Logger.getLogger(BaseParser.class);

	private static final byte ASCII_ZERO = 48;
	private static final byte ASCII_NINE = 57;

	protected SeekableStream source;
	private Token token;

	public BaseParser(SeekableStream stream) throws IOException {
		if(stream == null) {
			throw new IOException("Can't create SeekableStream, passed seekableeStream is null");
		}
		this.source = stream;
	}

	public BaseParser(String fileName) throws FileNotFoundException {
		if(fileName == null) {
			throw new FileNotFoundException("Can't create SeekableStream from file, filename is null");
		}
		this.source = new InternalInputStream(fileName);
	}

	public BaseParser(InputStream fileStream) throws IOException {
		if (fileStream == null) {
			throw new IOException("Can't create SeekableStream, fileStream is null");
		}
		this.source = SeekableStream.getSeekableStream(fileStream);
	}

	public void closeInputStream() throws IOException {
		this.source.close();
	}

	// PROTECTED METHODS

	protected Token getToken() {
		return this.token;
	}

	protected String getLine() throws IOException {
		initializeToken();
		this.token.clearValue();
		byte ch = this.source.readByte();
		while (!this.source.isEOF()) {
			if (ch == ASCII_LF || ch == ASCII_CR) {
				break;
			}
			appendToToken(ch);
			ch = this.source.readByte();
		}
		return this.token.getValue();
	}

	protected String getLine(final int offset) throws IOException {
		initializeToken();
		this.token.clearValue();
		this.source.seek(offset);
		byte ch = this.source.readByte();
		while (!this.source.isEOF()) {
			if (ch == ASCII_LF || ch == ASCII_CR) {
				break;
			}
			appendToToken(ch);
			ch = this.source.readByte();
		}
		return this.token.getValue();
	}

	protected String readUntilWhitespace() throws IOException {
		initializeToken();
		this.token.clearValue();
		byte ch = this.source.readByte();
		while (!isSpace(ch)) {
			appendToToken(ch);
			if (!this.source.isEOF()) {
				ch = this.source.readByte();
			} else {
				break;
			}
		}
		if (isSpace(ch)) {
			this.source.unread();
		}
		return this.token.getValue();
	}

	protected boolean findKeyword(final Token.Keyword keyword) throws IOException {
		nextToken();
		while (this.token.type != Token.Type.TT_EOF && (this.token.type != Token.Type.TT_KEYWORD || this.token.keyword != keyword)) {
			nextToken();
		}
		return this.token.type == Token.Type.TT_KEYWORD && this.token.keyword == keyword;
	}

	// lookUpSize starts from current offset
	protected boolean findKeyword(final Token.Keyword keyword, final int lookUpSize) throws IOException {
		long endOffset = this.source.getOffset() + lookUpSize < this.source.getStreamLength()
				? this.source.getOffset() + lookUpSize : this.source.getStreamLength();

		nextToken();
		while (this.token.type != Token.Type.TT_EOF && (this.token.type != Token.Type.TT_KEYWORD || this.token.keyword != keyword)) {
			if (this.source.getOffset() >= endOffset) {
				break;
			}
			nextToken();
		}
		return this.token.type == Token.Type.TT_KEYWORD && this.token.keyword == keyword;
	}

	protected void nextToken() throws IOException {
		skipSpaces(true);
		if (this.source.isEOF()) {
			this.token.type = Token.Type.TT_EOF;
			return;
		}

		this.token.type = Token.Type.TT_NONE;

		byte ch = this.source.readByte();

		switch (ch) {
			case '(':
				this.token.type = Token.Type.TT_LITSTRING;
				readLitString();
				break;
			case ')':
				//error
				break;
			case '<':
				ch = source.readByte();
				if (ch == '<') {
					this.token.type = Token.Type.TT_OPENDICT;
				} else {
					this.source.unread();
					this.token.type = Token.Type.TT_HEXSTRING;
					readHexString();
				}
				break;
			case '>':
				ch = this.source.readByte();
				if (ch == '>') {
					this.token.type = Token.Type.TT_CLOSEDICT;
				} else {
					// error
				}
				break;
			case '[':
				this.token.type = Token.Type.TT_OPENARRAY;
				break;
			case ']':
				this.token.type = Token.Type.TT_CLOSEARRAY;
				break;
			case '{': // as delimiter in PostScript calculator functions 181
				break;
			case '}':
				break;
			case '/':
				this.token.type = Token.Type.TT_NAME;
				readName();
				break;
			case '0':
			case '1':
			case '2':
			case '3':
			case '4':
			case '5':
			case '6':
			case '7':
			case '8':
			case '9':
			case '.':
				this.source.unread();
				readNumber();
				break;
			case '-':
				readNumber();
				this.token.integer = -this.token.integer;
				this.token.real = -this.token.real;
				break;
			default:
				this.source.unread();
				readToken();
				this.token.toKeyword();
				if (this.token.keyword == Token.Keyword.KW_NONE) {
					this.token.type = Token.Type.TT_NONE;
				}
				break;
		}
	}

	public ASInputStream getRandomAccess(final long length) throws IOException {
		skipEOL();
		ASInputStream result =
				this.source.getStream(this.source.getOffset(), length);
		source.seekFromCurrentPosition(length);
		return result;
    }

	protected boolean isNextByteEOL() throws IOException {
		byte c = (byte) this.source.peek();
		return isLF(c) || isCR(c);
	}

	protected void skipSingleEol() throws IOException {
		byte c = this.source.readByte();
		if (isCR(c)) {
			c = this.source.readByte();
			if (!isLF(c)) {
				this.source.unread();
			}
		} else if (!isLF(c)) {
			this.source.unread();
		}
	}

	protected void skipSpaces() throws IOException {
		this.skipSpaces(false);
	}

	protected void skipSpaces(boolean skipComment) throws IOException {
		byte ch;
		while (!this.source.isEOF()) {
			ch = this.source.readByte();
			if (CharTable.isSpace(ch)) {
				continue;
			}
			if (ch == '%' && skipComment) {
				skipComment();
				continue;
			}

			this.source.unread();
			break;
		}
	}

	protected void skipStreamSpaces() throws IOException {
		byte space = this.source.readByte();

		//check for whitespace
		while (space == ASCII_SPACE) {
			space = this.source.readByte();
		}

		if (space == ASCII_CR) {
			space = this.source.readByte();
			if (space != ASCII_LF) {
				this.source.unread();
			}
		} else if (space != ASCII_LF) {
			this.source.unread();
		}
	}

	protected boolean isDigit() throws IOException {
		return isDigit((byte) this.source.peek());
	}

	protected static boolean isDigit(byte c) {
		return c >= ASCII_ZERO && c <= ASCII_NINE;
	}

	protected boolean isHexDigit(byte ch) {
		return isDigit(ch)
				|| (ch >= 'a' && ch <= 'f')
				|| (ch >= 'A' && ch <= 'F');
	}

	protected static boolean isLF(int c) {
		return ASCII_LF == c;
	}

	protected static boolean isCR(int c) {
		return ASCII_CR == c;
	}

	// PRIVATE METHODS

	private void skipEOL() throws IOException {
		// skips EOL == { CR, LF, CRLF } only if it is the first symbol(s)
		byte ch = this.source.readByte();
		if (isLF(ch)) {
			return; // EOL == LF
		}

		if (isCR(ch)) {
			ch = this.source.readByte();
			if (isLF(ch)) {
				return; // EOL == CRLF
				// else EOL == CR and ch == next character
			}
		}

		this.source.unread();
	}

	private void skipComment() throws IOException {
		// skips all characters till EOL == { CR, LF, CRLF }
		byte ch;
		while (!this.source.isEOF()) {
			ch = this.source.readByte();
			if (isLF(ch)) {
				return; // EOL == LF
			}

			if (isCR(ch)) {
				ch = this.source.readByte();
				if (isLF(ch)) { // EOL == CR
					this.source.unread();
				} // else EOL == CRLF
				return;
			}
			// else skip regular character
		}
	}

	private void readLitString() throws IOException {
		this.token.clearValue();

		int parenthesesDepth = 0;

		byte ch = this.source.readByte();
		while (!this.source.isEOF()) {
			switch (ch) {
				default:
					appendToToken(ch);
					break;
				case '(':
					parenthesesDepth++;
					appendToToken(ch);
					break;
				case ')':
					if (parenthesesDepth == 0) {
						return;
					}

					parenthesesDepth--;
					appendToToken(ch);
					break;
				case '\\': {
					ch = this.source.readByte();
					switch (ch) {
						case '(':
							appendToToken(CharTable.ASCII_LEFT_PAR);
							break;
						case ')':
							appendToToken(CharTable.ASCII_RIGHT_PAR);
							break;
						case 'n':
							appendToToken(ASCII_LF);
							break;
						case 'r':
							appendToToken(ASCII_CR);
							break;
						case 't':
							appendToToken(CharTable.ASCII_HT);
							break;
						case 'b':
							appendToToken(CharTable.ASCII_BS);
							break;
						case 'f':
							appendToToken(CharTable.ASCII_FF);
							break;
						case '0':
						case '1':
						case '2':
						case '3':
						case '4':
						case '5':
						case '6':
						case '7': {
							// look for 1, 2, or 3 octal characters
							char ch1 = (char) (ch - '0');
							for (int i = 1; i < 3; i++) {
								ch = this.source.readByte();
								if (ch < '0' || ch > '7') {
									this.source.unread();
									break;
								} else {
									ch1 = (char) ((ch1 << 3) + (ch - '0'));
								}
							}
							appendToToken(ch1);
							break;
						}
						case ASCII_LF:
							break;
						case ASCII_CR:
							ch = this.source.readByte();
							if (ch != ASCII_LF) {
								this.source.unread();
							}
							break;
						default:
							appendToToken(ch);
							break;
					}
					break;
				}
			}

			ch = source.readByte();
		}
	}

	private void readHexString() throws IOException {
		this.token.clearValue();
		byte ch;
		int uc = 0;
		int hex;

		//these are required for pdf/a validation
		boolean containsOnlyHex = true;
		long hexCount = 0;

		boolean odd = false;
		while (!this.source.isEOF()) {
			ch = this.source.readByte();
			if (CharTable.isSpace(ch)) {
				continue;
			} else if (ch == '>') {
				if (odd) {
					uc <<= 4;
					appendToToken(uc);
				}
				this.token.setContainsOnlyHex(containsOnlyHex);
				this.token.setHexCount(hexCount);
				return;
			} else {
				hex = COSFilterASCIIHexDecode.decodeLoHex(ch);
				hexCount++;
				if (hex < 16 && hex > -1) { // skip all non-Hex characters
					if (odd) {
						uc = (uc << 4) + hex;
						appendToToken(uc);
						uc = 0;
					} else {
						uc = hex;
					}
					odd = !odd;
				} else {
					containsOnlyHex = false;
				}
			}
		}

		this.token.setContainsOnlyHex(containsOnlyHex);
		this.token.setHexCount(hexCount);
	}

	private void readName() throws IOException {
		this.token.clearValue();
		byte ch;
		while (!this.source.isEOF()) {
			ch = this.source.readByte();
			if (CharTable.isTokenDelimiter(ch)) {
				this.source.unread();
				break;
			}

			if (ch == '#') {
				byte ch1, ch2;
				int dc;
				ch1 = this.source.readByte();
				if (!source.isEOF() && COSFilterASCIIHexDecode.decodeLoHex(ch1) != COSFilterASCIIHexDecode.er) {
					dc = COSFilterASCIIHexDecode.decodeLoHex(ch1);
					ch2 = this.source.readByte();
					if (!this.source.isEOF() && COSFilterASCIIHexDecode.decodeLoHex(ch2) != COSFilterASCIIHexDecode.er) {
						dc = ((dc << 4) + COSFilterASCIIHexDecode.decodeLoHex(ch2));
						appendToToken(dc);
					} else {
						appendToToken(ch);
						appendToToken(ch1);
						this.source.unread();
					}
				} else {
					appendToToken(ch);
					this.source.unread();
				}
			} else {
				appendToToken(ch);
			}
		}
	}

	private void readToken() throws IOException {
		this.token.clearValue();
		byte ch;
		while (!this.source.isEOF()) {
			ch = this.source.readByte();
			if (CharTable.isTokenDelimiter(ch)) {
				this.source.unread();
				break;
			}

			appendToToken(ch);
		}
	}

	protected void readNumber() throws IOException {
		try {
			initializeToken();
			this.token.clearValue();
			this.token.type = Token.Type.TT_INTEGER;
			byte ch;
			while (!this.source.isEOF()) {
				ch = this.source.readByte();
				if (CharTable.isTokenDelimiter(ch)) {
					this.source.unread();
					break;
				}
				if (ch >= '0' && ch <= '9') {
					appendToToken(ch);
				} else if (ch == '.') {
					this.token.type = Token.Type.TT_REAL;
					appendToToken(ch);
				} else {
					this.source.unread();
					break;
				}
			}
			if (this.token.type == Token.Type.TT_INTEGER) {
				long value = Long.valueOf(this.token.getValue());
				this.token.integer = value;
				this.token.real = (double) value;
			} else {
				double value = Double.valueOf(this.token.getValue());
				this.token.integer = Math.round(value);
				this.token.real = value;
			}
		} catch (NumberFormatException e) {
			LOGGER.debug(e);
		}
	}

	protected void initializeToken() {
		if (this.token == null) {
			this.token = new Token();
		}
	}

	private void appendToToken(final byte ch) {
		this.token.append((char) ch);
	}

	private void appendToToken(final int ch) {
		this.token.append((char) ch);
	}

	public static byte[] getRawBytes(String string) {
		byte[] res = new byte[string.length()];
		for (int i = 0; i < string.length(); ++i) {
			res[i] = (byte) string.charAt(i);
		}
		return res;
	}

	protected void skipExpectedCharacter(char exp) throws IOException {
		char c = (char) this.source.readByte();
		if(c != exp) {
			throw new IOException("Unexpected character on byte " + (this.source.getOffset() - 1) +
			"; expected " + exp + " but got " + c);
		}
	}
}
