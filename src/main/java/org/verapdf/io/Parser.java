package org.verapdf.io;

import org.verapdf.as.CharTable;
import org.verapdf.as.io.ASFileInStream;
import org.verapdf.as.io.ASInputStream;
import org.verapdf.cos.COSFilterASCIIHexDecode;

import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * @author Timur Kamalov
 */
public class Parser {

	private InternalInputStream stream;
	private Token token;

	public Parser(String fileName) throws FileNotFoundException {
		this.stream = new InternalInputStream(fileName);
	}

	public void closeInputStream() throws IOException {
		this.stream.close();
	}

	public long getOffset() throws IOException {
		return this.stream.getOffset();
	}

	public void seek(final long offset) throws IOException {
		this.stream.seek(offset);
	}

	public void seekFromEnd(final int offset) throws IOException {
		this.stream.seekFromEnd(offset);
	}

	public void seekFromCurrentPosition(final int offset) throws IOException {
		this.stream.seekFromCurrentPosition(offset);
	}

	// PROTECTED METHODS

	protected Token getToken() {
		return this.token;
	}

	protected String getLine(final int offset) throws IOException {
		initializeToken();
		this.stream.seek(offset);
		this.token.token = "";
		byte ch = this.stream.read();
		while (!this.stream.isEof()) {
			if (ch == CharTable.ASCII_LF || ch == CharTable.ASCII_CR) {
				break;
			}
			appendToToken(ch);
			ch = this.stream.read();
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
		skipSpaces();
		if (this.stream.isEof()) {
			this.token.type = Token.Type.TT_EOF;
			return;
		}

		this.token.type = Token.Type.TT_NONE;

		byte ch = this.stream.read();

		switch (ch) {
			case '(':
				this.token.type = Token.Type.TT_LITSTRING;
				readLitString();
				break;
			case ')':
				//error
				break;
			case '<':
				ch = stream.read();
				if (ch == '<') {
					this.token.type = Token.Type.TT_OPENDICT;
				} else {
					this.stream.unread();
					this.token.type = Token.Type.TT_HEXSTRING;
					readHexString();
				}
				break;
			case '>':
				ch = this.stream.read();
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
				this.stream.unread();
				readNumber();
				break;
			case '-':
				readNumber();
				this.token.integer = -this.token.integer;
				this.token.real = -this.token.real;
				break;
			default:
				this.stream.unread();
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
		ASInputStream result = new ASFileInStream(this.stream.getStream(), this.stream.getOffset(), length);
		stream.seekFromCurrentPosition(length);

		return result;
	}

	// PRIVATE METHODS

	private void skipSpaces() throws IOException {
		byte ch;
		while (!this.stream.isEof()) {
			ch = this.stream.read();
			if (CharTable.isSpace(ch)) {
				continue;
			}
			if (ch == '%') {
				skipComment();
				continue;
			}

			this.stream.unread();
			break;
		}
	}

	private void skipEOL() throws IOException {
		// skips EOL == { CR, LF, CRLF } only if it is the first symbol(s)
		byte ch = this.stream.read();
		if (ch == CharTable.ASCII_LF) {
			return; // EOL == LF
		}

		if (ch == CharTable.ASCII_CR) {
			ch = this.stream.read();
			if (ch == CharTable.ASCII_LF) {
				return; // EOL == CRLF
				// else EOL == CR and ch == next character
			}
		}

		this.stream.unread();
	}

	private void skipComment() throws IOException {
		// skips all characters till EOL == { CR, LF, CRLF }
		byte ch;
		while (!this.stream.isEof()) {
			ch = this.stream.read();
			if (ch == CharTable.ASCII_LF) {
				return; // EOL == LF
			}

			if (ch == CharTable.ASCII_CR) {
				ch = this.stream.read();
				if (ch != CharTable.ASCII_LF) { // EOL == CR
					this.stream.unread();
				} // else EOL == CRLF
				return;
			}
			// else skip regular character
		}
	}

	private void readLitString() throws IOException {
		this.token.token = "";

		int parenthesesDepth = 0;

		byte ch = this.stream.read();
		while (!this.stream.isEof()) {
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
					ch = this.stream.read();
					switch (ch) {
						case '(':
							appendToToken(CharTable.ASCII_LEFT_PAR);
							break;
						case ')':
							appendToToken(CharTable.ASCII_RIGHT_PAR);
							break;
						case 'n':
							appendToToken(CharTable.ASCII_LF);
							break;
						case 'r':
							appendToToken(CharTable.ASCII_CR);
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
								ch = this.stream.read();
								if (ch < '0' || ch > '7') {
									this.stream.unread();
									break;
								} else {
									ch1 = (char) ((ch1 << 3) + (ch - '0'));
								}
							}
							appendToToken(ch1);
							break;
						}
						case CharTable.ASCII_LF:
							break;
						case CharTable.ASCII_CR:
							ch = this.stream.read();
							if (ch != CharTable.ASCII_LF) {
								this.stream.unread();
							}
							break;
						default:
							appendToToken(ch);
							break;
					}
					break;
				}
			}

			ch = stream.read();
		}
	}

	private void readHexString() throws IOException {
		this.token.token = "";
		byte ch;
		int uc = 0;
		int hex;

		boolean odd = false;
		while (!this.stream.isEof()) {
			ch = this.stream.read();
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
				if (hex < 16) { // skip all non-Hex characters
					if (odd) {
						uc = (uc << 4) + hex;
						appendToToken(uc);
						uc = 0;
					} else {
						uc = hex;
					}
					odd = !odd;
				}
			}
		}
	}

	private void readName() throws IOException {
		this.token.token = "";
		byte ch;
		while (!this.stream.isEof()) {
			ch = this.stream.read();
			if (CharTable.isTokenDelimiter(ch)) {
				this.stream.unread();
				break;
			}

			if (ch == '#') {
				byte ch1, ch2;
				int dc;
				ch1 = this.stream.read();
				if (!stream.isEof() && COSFilterASCIIHexDecode.decodeLoHex(ch1) != COSFilterASCIIHexDecode.er) {
					dc = COSFilterASCIIHexDecode.decodeLoHex(ch1);
					ch2 = this.stream.read();
					if (!this.stream.isEof() && COSFilterASCIIHexDecode.decodeLoHex(ch2) != COSFilterASCIIHexDecode.er) {
						dc = ((dc << 4) + COSFilterASCIIHexDecode.decodeLoHex(ch2));
						appendToToken(dc);
					} else {
						appendToToken(ch);
						appendToToken(ch1);
						this.stream.unread();
					}
				} else {
					appendToToken(ch);
					this.stream.unread();
				}
			} else {
				appendToToken(ch);
			}
		}
	}

	private void readToken() throws IOException {
		this.token.token = "";
		byte ch;
		while (!this.stream.isEof()) {
			ch = this.stream.read();
			if (CharTable.isTokenDelimiter(ch)) {
				this.stream.unread();
				break;
			}

			appendToToken(ch);
		}
	}

	private void readNumber() throws IOException {
		this.token.token = "";
		this.token.type = Token.Type.TT_INTEGER;
		byte ch;
		while (!this.stream.isEof()) {
			ch = this.stream.read();
			if (CharTable.isTokenDelimiter(ch)) {
				this.stream.unread();
				break;
			}
			if (ch >= '0' && ch <= '9') {
				appendToToken(ch);
			} else if (ch == '.') {
				this.token.type = Token.Type.TT_REAL;
				appendToToken(ch);
			} else {
				this.stream.unread();
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
