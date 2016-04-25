package org.verapdf.io;

import org.verapdf.as.CharTable;
import org.verapdf.as.io.ASFileInStream;
import org.verapdf.as.io.ASInputStream;
import org.verapdf.cos.COSFilterASCIIHexDecode;

import java.io.FileNotFoundException;
import java.io.IOException;

import static org.verapdf.as.CharTable.ASCII_CR;
import static org.verapdf.as.CharTable.ASCII_LF;

/**
 * @author Timur Kamalov
 */
public class Parser {

	private static final byte ASCII_ZERO = 48;
	private static final byte ASCII_NINE = 57;

	private InternalInputStream source;
	private Token token;

	public Parser(String fileName) throws FileNotFoundException {
		this.source = new InternalInputStream(fileName);
	}

	public void closeInputStream() throws IOException {
		this.source.close();
	}

	public long getSourceLength() throws IOException {
		return this.source.getStreamLength();
	}

	public long getOffset() throws IOException {
		return this.source.getOffset();
	}

	public int read(byte[] buffer, int size) throws IOException {
		return this.source.read(buffer, size);
	}

	public byte readByte() throws IOException {
		return this.source.read();
	}

	public byte peek() throws IOException {
		return this.source.peek();
	}

	public void unread() throws IOException{
		this.source.unread();
	}

	public void unread(final int count) throws IOException{
		this.source.unread(count);
	}

	public void seek(final long offset) throws IOException {
		this.source.seek(offset);
	}

	public void seekFromEnd(final int offset) throws IOException {
		this.source.seekFromEnd(offset);
	}

	public void seekFromCurrentPosition(final int offset) throws IOException {
		this.source.seekFromCurrentPosition(offset);
	}

	public boolean isEof() throws IOException {
		return this.source.isEof();
	}

	// PROTECTED METHODS

	protected Token getToken() {
		return this.token;
	}

	protected String getLine() throws IOException {
		initializeToken();
		this.token.token = "";
		byte ch = this.source.read();
		while (!this.source.isEof()) {
			if (ch == ASCII_LF || ch == ASCII_CR) {
				break;
			}
			appendToToken(ch);
			ch = this.source.read();
		}
		return this.token.token;
	}

	protected String getLine(final int offset) throws IOException {
		initializeToken();
		this.source.seek(offset);
		this.token.token = "";
		byte ch = this.source.read();
		while (!this.source.isEof()) {
			if (ch == ASCII_LF || ch == ASCII_CR) {
				break;
			}
			appendToToken(ch);
			ch = this.source.read();
		}
		return this.token.token;
	}

	protected boolean findKeyword(final Token.Keyword keyword) throws IOException {
		nextToken();
		while (this.token.type != Token.Type.TT_EOF && (this.token.type != Token.Type.TT_KEYWORD || this.token.keyword != keyword)) {
			nextToken();
		}
		return this.token.type == Token.Type.TT_KEYWORD && this.token.keyword == keyword;
	}

	protected void nextToken() throws IOException {
		skipSpaces(true);
		if (this.source.isEof()) {
			this.token.type = Token.Type.TT_EOF;
			return;
		}

		this.token.type = Token.Type.TT_NONE;

		byte ch = this.source.read();

		switch (ch) {
			case '(':
				this.token.type = Token.Type.TT_LITSTRING;
				readLitString();
				break;
			case ')':
				//error
				break;
			case '<':
				ch = source.read();
				if (ch == '<') {
					this.token.type = Token.Type.TT_OPENDICT;
				} else {
					this.source.unread();
					this.token.type = Token.Type.TT_HEXSTRING;
					readHexString();
				}
				break;
			case '>':
				ch = this.source.read();
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

	public ASInputStream getStream(final long length) throws IOException {
		skipEOL();
		ASInputStream result = new ASFileInStream(this.source.getStream(), this.source.getOffset(), length);
		source.seekFromCurrentPosition(length);

		return result;
	}

	protected boolean isNextByteEOL() throws IOException {
		byte c = this.source.peek();
		return isLF(c) || isCR(c);
	}

	protected void skipSingleEol() throws IOException {
		byte c = this.source.read();
		if (isCR(c)) {
			c = this.source.read();
			if (!isLF(c)) {
				this.source.unread();
			}
		} else if (!isLF(c)) {
			this.source.unread();
		}
	}

	protected void skipSpaces(boolean skipComment) throws IOException {
		byte ch;
		while (!this.source.isEof()) {
			ch = this.source.read();
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

	protected boolean isDigit() throws IOException {
		return isDigit(peek());
	}

	protected boolean isDigit(byte c) {
		return c >= ASCII_ZERO && c <= ASCII_NINE;
	}

	protected boolean isHexDigit(byte ch) {
		return isDigit(ch)
				|| (ch >= 'a' && ch <= 'f')
				|| (ch >= 'A' && ch <= 'F');
	}

	protected boolean isLF(int c) {
		return ASCII_LF == c;
	}

	protected boolean isCR(int c) {
		return ASCII_CR == c;
	}

	// PRIVATE METHODS

	private void skipEOL() throws IOException {
		// skips EOL == { CR, LF, CRLF } only if it is the first symbol(s)
		byte ch = this.source.read();
		if (isLF(ch)) {
			return; // EOL == LF
		}

		if (isCR(ch)) {
			ch = this.source.read();
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
		while (!this.source.isEof()) {
			ch = this.source.read();
			if (isLF(ch)) {
				return; // EOL == LF
			}

			if (isCR(ch)) {
				ch = this.source.read();
				if (isLF(ch)) { // EOL == CR
					this.source.unread();
				} // else EOL == CRLF
				return;
			}
			// else skip regular character
		}
	}

	private void readLitString() throws IOException {
		this.token.token = "";

		int parenthesesDepth = 0;

		byte ch = this.source.read();
		while (!this.source.isEof()) {
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
					ch = this.source.read();
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
								ch = this.source.read();
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
							ch = this.source.read();
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

			ch = source.read();
		}
	}

	private void readHexString() throws IOException {
		this.token.token = "";
		byte ch;
		int uc = 0;
		int hex;

		//these are required for pdf/a validation
		boolean containsOnlyHex = true;
		long hexCount = 0;

		boolean odd = false;
		while (!this.source.isEof()) {
			ch = this.source.read();
			if (CharTable.isSpace(ch)) {
				continue;
			} else if (ch == '>') {
				if (odd) {
					uc <<= 4;
					appendToToken(uc);
				}
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
		this.token.token = "";
		byte ch;
		while (!this.source.isEof()) {
			ch = this.source.read();
			if (CharTable.isTokenDelimiter(ch)) {
				this.source.unread();
				break;
			}

			if (ch == '#') {
				byte ch1, ch2;
				int dc;
				ch1 = this.source.read();
				if (!source.isEof() && COSFilterASCIIHexDecode.decodeLoHex(ch1) != COSFilterASCIIHexDecode.er) {
					dc = COSFilterASCIIHexDecode.decodeLoHex(ch1);
					ch2 = this.source.read();
					if (!this.source.isEof() && COSFilterASCIIHexDecode.decodeLoHex(ch2) != COSFilterASCIIHexDecode.er) {
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
		this.token.token = "";
		byte ch;
		while (!this.source.isEof()) {
			ch = this.source.read();
			if (CharTable.isTokenDelimiter(ch)) {
				this.source.unread();
				break;
			}

			appendToToken(ch);
		}
	}

	private void readNumber() throws IOException {
		this.token.token = "";
		this.token.type = Token.Type.TT_INTEGER;
		byte ch;
		while (!this.source.isEof()) {
			ch = this.source.read();
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
			int value = Integer.valueOf(this.token.token);
			this.token.integer = value;
			this.token.real = (double) value;
		} else {
			double value = Double.valueOf(this.token.token);
			this.token.integer = Math.round(value);
			this.token.real = value;
		}
	}

	private void initializeToken() {
		if (this.token == null) {
			this.token = new Token();
		}
	}

	private void appendToToken(final byte ch) {
		this.token.token += (char) ch;
	}

	private void appendToToken(final int ch) {
		this.token.token += (char) ch;
	}

}
