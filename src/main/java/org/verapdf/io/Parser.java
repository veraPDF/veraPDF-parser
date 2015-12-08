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
					int ch1 = ch - '0';
					for (int i = 1; i < 3; i++) {
						this.stream.get(ch);
						if (ch < '0' || ch > '7') {
							this.stream.unread();
							break;
						} else {
							ch1 = (ch1 << 3) + (ch - '0');
						}
					}
					//TODO : pushback
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
			char ch1, ch2, dc = 0;
			if (stream.get(ch1) != null)

		}
	}



}
