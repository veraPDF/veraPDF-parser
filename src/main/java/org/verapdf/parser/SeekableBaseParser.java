/**
 * This file is part of veraPDF Parser, a module of the veraPDF project.
 * Copyright (c) 2015-2024, veraPDF Consortium <info@verapdf.org>
 * All rights reserved.
 *
 * veraPDF Parser is free software: you can redistribute it and/or modify
 * it under the terms of either:
 *
 * The GNU General public license GPLv3+.
 * You should have received a copy of the GNU General Public License
 * along with veraPDF Parser as the LICENSE.GPL file in the root of the source
 * tree.  If not, see http://www.gnu.org/licenses/ or
 * https://www.gnu.org/licenses/gpl-3.0.en.html.
 *
 * The Mozilla Public License MPLv2+.
 * You should have received a copy of the Mozilla Public License along with
 * veraPDF Parser as the LICENSE.MPL file in the root of the source tree.
 * If a copy of the MPL was not distributed with this file, you can obtain one at
 * http://mozilla.org/MPL/2.0/.
 */
package org.verapdf.parser;

import org.verapdf.as.io.ASInputStream;
import org.verapdf.io.InternalInputStream;
import org.verapdf.io.SeekableInputStream;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import static org.verapdf.as.CharTable.*;

/**
 * @author Timur Kamalov
 */
public class SeekableBaseParser extends BaseParser {

	public SeekableBaseParser(SeekableInputStream stream) throws IOException {
		if (stream == null) {
			throw new IOException("Can't create SeekableStream, passed seekableStream is null");
		}
		this.source = stream;
	}

	public SeekableBaseParser(String fileName) throws IOException {
		if (fileName == null) {
			throw new FileNotFoundException("Can't create SeekableStream from file, filename is null");
		}
		this.source = new InternalInputStream(fileName);
	}

	public SeekableBaseParser(InputStream fileStream) throws IOException {
		if (fileStream == null) {
			throw new IOException("Can't create SeekableStream, fileStream is null");
		}
		if (fileStream instanceof SeekableInputStream) {
			this.source = (SeekableInputStream) fileStream;
		} else {
			try {
				this.source = SeekableInputStream.getSeekableStream(fileStream);
			} finally {
				fileStream.close();
			}
		}
	}

	public void closeInputStream() throws IOException {
		this.source.close();
	}

	// PROTECTED METHODS

	protected String getLine(final int offset) throws IOException {
		initializeToken();
		this.token.clearValue();
		this.getSource().seek(offset);
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

	// lookUpSize starts from current offset
	@Override
	protected boolean findKeyword(final Token.Keyword keyword, final int lookUpSize) throws IOException {
		long endOffset = Math.min(this.getSource().getOffset() + lookUpSize, this.getSource().getStreamLength());

		nextToken();
		while (this.token.type != Token.Type.TT_EOF && (this.token.type != Token.Type.TT_KEYWORD || this.token.keyword != keyword)) {
			if (this.getSource().getOffset() >= endOffset) {
				break;
			}
			nextToken();
		}
		return this.token.type == Token.Type.TT_KEYWORD && this.token.keyword == keyword;
	}

	public ASInputStream getRandomAccess(final long length) throws IOException {
		ASInputStream result =
				this.getSource().getStream(this.getSource().getOffset(), length);
		getSource().seekFromCurrentPosition(length);
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
		}
		if (!isLF(c)) {
			this.source.unread();
		}
	}

	protected void skipSingleSpace() throws IOException {
		this.skipSingleSpace(false);
	}

	protected static boolean isHexDigit(byte ch) {
		return isDigit(ch)
				|| (ch >= 'a' && ch <= 'f')
				|| (ch >= 'A' && ch <= 'F');
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

	@Override
	protected void skipComment() throws IOException {
		// skips all characters till EOL == { CR, LF, CRLF }
		while (!this.source.isEOF()) {
			byte ch = this.source.readByte();
			if (isEOL(ch)) {
				return;
			}
			// else skip regular character
		}
	}

	protected void nextLine() throws IOException {
		while (!this.source.isEOF()) {
			byte ch = this.source.readByte();
			if (isEOL(ch)) {
				skipEOL();
				return;
			}
		}
	}

	protected boolean isEOL(byte ch) throws IOException {
		if (isLF(ch)) {
			return true; // EOL == LF
		} else if (isCR(ch)) {
			ch = this.source.readByte();
			if (!isLF(ch)) { // EOL == CR
				this.source.unread();
			} // else EOL == CRLF
			return true;
		} else {
			return false;
		}
	}

	@Override
	protected void readASCII85() throws IOException {
		long ascii85Start = this.getSource().getOffset();
		long ascii85End = this.getSource().getStreamLength();
		byte b = this.source.readByte();
		while (!source.isEOF()) {
			if (b == '~' && this.source.peek() == '>') {
				ascii85End = this.getSource().getOffset() - 1;
				this.source.readByte();	// here we finished reading all ascii85 string
				break;
			}
			b = source.readByte();
		}
		try (ASInputStream ascii85 = this.getSource().getStream(ascii85Start, ascii85End - ascii85Start)) {
			decodeASCII85(ascii85, (int) (ascii85End - ascii85Start));
		}
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
		if (c != exp) {
			throw new IOException(getErrorMessage("Unexpected character: expected " + exp + " but got " + c,
					this.getSource().getCurrentOffset() - 1));
		}
	}

	@Override
	protected String getErrorMessage(String message) {
		return getErrorMessage(message, getSource().getCurrentOffset());
	}

	protected String getErrorMessage(String message, long offset) {
		return message + "(offset = " + offset + ')';
	}

	@Override
	protected SeekableInputStream getSource() {
		return (SeekableInputStream) source;
	}
}
