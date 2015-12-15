package org.verapdf.io;

import org.verapdf.as.CharTable;

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

	public int getOffset() throws IOException {
		return this.stream.tellg();
	}

	public void seek(final int offset) throws IOException {
		this.stream.seekg(offset);
	}


	// PROTECTED METHODS

	protected Token getToken() {
		return this.token;
	}

	protected String getLine(final int offset) throws IOException {
		this.token.token = "";
		char ch = 0;
		while (stream.get(ch) != null) {
			if (ch == CharTable.ASCII_LF || ch == CharTable.ASCII_CR) {
				break;
			}
			this.token.token.concat(Character.toString(ch));
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
		if (this.stream != null) {
			this.token.type = Token.Type.TT_EOF;
			return;
		}

		this.token.type = Token.Type.TT_NONE;

		char ch = 0;
		this.stream.get(ch);

		switch (ch) {
			case '(':
				this.token.type = Token.Type.TT_LITSTRING;
				readLitString();
				break;
			case ')':
				//error
				break;
			case '<':
				stream.get(ch);
				if (ch == '<') {
					this.token.type = Token.Type.TT_OPENDICT;
				} else {
					this.stream.unread();
					this.token.type = Token.Type.TT_CLOSEDICT;
					readHexString();
				}
				break;
			case '>':
				this.stream.get(ch);
				if (ch == '>') {
					this.token.type = Token.Type.TT_CLOSEDICT;
				} else {
					// error
				}
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
			case '0':case '1':case '2':case '3':case '4':
			case '5':case '6':case '7':case '8':case'9':
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





	// PRIVATE METHODS

	private void skipSpaces() throws IOException {
		char ch = 0;
		while (this.stream.get(ch) != null) {
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
		char ch = 0;
		if (this.stream.get(ch) != null) {
			if (ch == CharTable.ASCII_LF) {
				return; // EOL == LF
			}

			if (ch == CharTable.ASCII_CR) {
				if (this.stream.get(ch) != null) {
					if (ch == CharTable.ASCII_LF) {
						return; // EOL == CRLF
						// else EOL == CR and ch == next character
					}
				}
			}

			this.stream.unread();
		}
	}

	private void skipComment() throws IOException {
		// skips all characters till EOL == { CR, LF, CRLF }
		char ch = 0;
		while (this.stream.get(ch) != null) {
			if (ch == CharTable.ASCII_LF) {
				return; // EOL == LF
			}

			if (ch == CharTable.ASCII_CR) {
				if (this.stream.get(ch) != null) {
					if (ch != CharTable.ASCII_LF) { // EOL == CR
						this.stream.unread();
					} // else EOL == CRLF
				}
				return;
			}
			// else skip regular character
		}
	}

	private void readLitString() throws IOException {
		this.token.token = "";

		int parenthesesDepth = 0;

		char ch = 0;
		while (this.stream.get(ch) != null) {
			switch (ch) {
				case 'n':
					this.token.token.concat(Byte.toString(CharTable.ASCII_LF));
					break;
				case 'r':
					this.token.token.concat(Byte.toString(CharTable.ASCII_CR));
					break;
				case 't':
					this.token.token.concat(Byte.toString(CharTable.ASCII_HT));
					break;
				case 'b':
					this.token.token.concat(Byte.toString(CharTable.ASCII_BS));
					break;
				case 'f':
					this.token.token.concat(Byte.toString(CharTable.ASCII_FF));
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
						this.stream.get(ch);
						if (ch < '0' || ch > '7') {
							this.stream.unread();
							break;
						} else {
							ch1 = (char) ((ch1 << 3) + (ch - '0'));
						}
					}
					this.token.token.concat(Character.toString(ch1));
					break;
				}
				case CharTable.ASCII_LF:
					break;
				case CharTable.ASCII_CR:
					this.stream.get(ch);
					if (ch != CharTable.ASCII_LF) {
						this.stream.unread();
					}
					break;
				default:
					this.token.token.concat(Character.toString(ch));
			}
 		}
	}

	private void readHexString() throws IOException {
		this.token.token = "";
		char ch = 0;
		char uc = 0;
		char hex = 0;

		boolean odd = false;
		while (this.stream.get(ch) != null) {
			if (CharTable.isSpace(ch)) {
				continue;
			} else if (ch == '>') {
				if (odd) {
					uc = (char) ((uc << 4) + hex);
					this.token.token.concat(Character.toString(uc));
				}
				return;
			} else {
				//TODO : hex = COSFilterASCIIHexDecode::DecodeLoHex(ch);
				if (hex < 16) { // skip all non-Hex characters
					if (odd) {
						uc = (char) ((uc << 4) + hex);
						this.token.token.concat(Character.toString(uc));
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
		char ch = 0;
		while (this.stream.get(ch) != null) {
			if (CharTable.isTokenDelimiter(ch)) {
				this.stream.unread();
				break;
			}
		}

		if (ch == '#') {
			char ch1 = 0, ch2 = 0, dc = 0;
			if (stream.get(ch1) != null) { //TODO : && COSFilterASCIIHexDecode::DecodeLoHex(ch1) != COSFilterASCIIHexDecode::er
				dc = ch1; //TODO : COSFilterASCIIHexDecode::DecodeLoHex(ch1);
				if (this.stream.get(ch1) != null) { //TODO : && COSFilterASCIIHexDecode::DecodeLoHex(ch1) != COSFilterASCIIHexDecode::er)
					dc = ch1; //TODO : COSFilterASCIIHexDecode::DecodeLoHex(ch1);
					if (this.stream.get(ch2) != null) { //TODO : && COSFilterASCIIHexDecode::DecodeLoHex(ch2) != COSFilterASCIIHexDecode::er
						dc = (char) ((dc << 4) + ch2); //TODO : COSFilterASCIIHexDecode::DecodeLoHex(ch2);
						this.token.token.concat(Character.toString(dc));
					} else {
						this.token.token.concat(Character.toString(ch));
						this.token.token.concat(Character.toString(ch1));
						this.stream.unread();
					}
				}
			} else {
				this.token.token.concat(Character.toString(ch));
				this.stream.unread();
			}
		} else {
			this.token.token.concat(Character.toString(ch));
		}
	}

	private void readToken() throws IOException {
		this.token.token = "";
		char ch = 0;
		while (this.stream.get(ch) != null) {
			if (CharTable.isTokenDelimiter(ch)) {
				this.stream.unread();
				break;
			}

			this.token.token.concat(Character.toString(ch));
		}
	}

	private void readNumber() throws IOException {
		this.token.token = "";
		this.token.type = Token.Type.TT_INTEGER;
		char ch = 0;
		while (this.stream.get(ch) != null) {
			if (CharTable.isTokenDelimiter(ch)) {
				this.stream.unread();
				break;
			}
			if (ch >= '0' && ch <= '9') {
				this.token.token.concat(Character.toString(ch));
			} else if (ch == '.') {
				this.token.type = Token.Type.TT_REAL;
			} else {
				this.stream.unread();
				break;
			}
		}
		this.token.integer = Integer.valueOf(this.token.token);
		this.token.real = Float.parseFloat(this.token.token);
	}

}
