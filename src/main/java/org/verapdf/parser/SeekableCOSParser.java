/**
 * This file is part of veraPDF Parser, a module of the veraPDF project.
 * Copyright (c) 2015-2025, veraPDF Consortium <info@verapdf.org>
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

import org.verapdf.as.ASAtom;
import org.verapdf.as.io.ASInputStream;
import org.verapdf.cos.*;
import org.verapdf.exceptions.VeraPDFParserException;
import org.verapdf.io.InternalInputStream;
import org.verapdf.io.SeekableInputStream;
import org.verapdf.tools.resource.ASFileStreamCloser;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Timur Kamalov
 */
public class SeekableCOSParser extends COSParser {

	private static final Logger LOGGER = Logger.getLogger(SeekableCOSParser.class.getCanonicalName());
	
	private boolean isLengthParsing = false;

	public SeekableCOSParser(final SeekableInputStream seekableInputStream) throws IOException {
		super(new SeekableBaseParser(seekableInputStream));
	}

	public SeekableCOSParser(final String filename) throws IOException {
		super(new SeekableBaseParser(filename));
	}

	public SeekableCOSParser(final InputStream fileStream) throws IOException {
		super(new SeekableBaseParser(fileStream));
	}

	public SeekableCOSParser(final COSDocument document, final String filename) throws IOException { //tmp ??
		this(filename);
		this.document = document;
	}

	public SeekableCOSParser(final COSDocument document, final InputStream fileStream) throws IOException { //tmp ??
		this(fileStream);
		this.document = document;
	}

	@Override
	protected COSObject getDictionary() throws IOException {
		COSObject dict = super.getDictionary();
		if (dict.getType() != COSObjType.COS_DICT) {
			return dict;
		}

		final Token token = getBaseParser().getToken();

		long reset = this.getSource().getOffset();
		if (this.flag) {
			getBaseParser().nextToken();
		}
		this.flag = false;

		if (token.type == Token.Type.TT_KEYWORD &&
				token.keyword == Token.Keyword.KW_STREAM) {
			return getStream(dict);
		}
		this.getSource().seek(reset);
		this.flag = true;

		return dict;
	}

	protected COSObject getStream(COSObject dict) throws IOException {
		if (this.flag) {
			getBaseParser().nextToken();
		}
		this.flag = true;

		final Token token = getBaseParser().getToken();

		if (token.type != Token.Type.TT_KEYWORD ||
				token.keyword != Token.Keyword.KW_STREAM) {
			this.flag = false;
			return dict;
		}

		checkStreamSpacings(dict);
		long streamStartOffset = getSource().getOffset();
		if (isLengthParsing) {
			throw new VeraPDFParserException(getErrorMessage("Incorrect type of Length value in stream dictionary"));
		}
		Long size = null;
		try {
			isLengthParsing = true;
			COSObject length = dict.getKey(ASAtom.LENGTH);
			size = length.getInteger();
		} catch (Exception exception) {
			LOGGER.log(Level.WARNING, exception.getMessage());
		} finally {
			isLengthParsing = false;
		}
		getSource().seek(streamStartOffset);

		boolean streamLengthValid = checkStreamLength(size);

		if (streamLengthValid) {
			dict.setRealStreamSize(size);
			ASInputStream stm = getBaseParser().getRandomAccess(size);
			dict.setData(stm);
			if (stm instanceof InternalInputStream) {
				this.document.addFileResource(new ASFileStreamCloser(stm));
			}
		} else {
			//trying to find endstream keyword
			long realStreamSize = -1;
			int bufferLength = 512;
			byte[] buffer = new byte[bufferLength];
			int eolLength = 0;
			boolean isPrevCR = false;
			while (realStreamSize == -1 && !getSource().isEOF()) {
				long bytesRead = getSource().read(buffer, bufferLength);
				for (int i = 0; i < bytesRead; i++) {
					if (buffer[i] == 101) {
						long reset = getSource().getOffset();
						long possibleEndStreamOffset = reset - bytesRead + i - eolLength;
						getSource().seek(possibleEndStreamOffset);
						getBaseParser().nextToken();
						if (token.type == Token.Type.TT_KEYWORD &&
								token.keyword == Token.Keyword.KW_ENDSTREAM) {
							realStreamSize = possibleEndStreamOffset - streamStartOffset;
							dict.setRealStreamSize(realStreamSize);
							getSource().seek(streamStartOffset);
							ASInputStream stm = getBaseParser().getRandomAccess(realStreamSize);
							dict.setData(stm);
							getSource().seek(possibleEndStreamOffset);
							if (stm instanceof InternalInputStream) {
								this.document.addFileResource(new ASFileStreamCloser(stm));
							}
							break;
						}
						getSource().seek(reset);
					}

					//we need to subtract eol before endstream length from stream length
					if (BaseParser.isCR(buffer[i])) {
						// if current byte is CR, then this is the 1st byte of eol
						eolLength = 1;
						isPrevCR = true;
					} else {
						if (BaseParser.isLF(buffer[i])) {
							eolLength = isPrevCR ? 2 : 1;
						} else {
							eolLength = 0;
						}
						isPrevCR = false;
					}
				}
			}
			if (realStreamSize == -1) {
				throw new IOException(getErrorMessage("End of stream is not found"));
			}
		}

		checkEndstreamSpacings(dict, streamStartOffset, size);

		return dict;
	}


	private void checkStreamSpacings(COSObject stream) throws IOException {
		byte whiteSpace = getSource().readByte();
		if (BaseParser.isCR(whiteSpace)) {
			whiteSpace = getSource().readByte();
			if (!BaseParser.isLF(whiteSpace)) {
				stream.setStreamKeywordCRLFCompliant(false);
				getSource().unread();
			}
		} else if (!BaseParser.isLF(whiteSpace)) {
			LOGGER.log(Level.WARNING, getErrorMessage("Stream has no EOL marker"));
			stream.setStreamKeywordCRLFCompliant(false);
			getSource().unread();
		}
	}

	private boolean checkStreamLength(Long streamLength) throws IOException {
		if (streamLength == null) {
			LOGGER.log(Level.WARNING, getErrorMessage("Stream length has wrong value or is missing"));
			return false;
		}
		boolean validLength = true;
		long start = getSource().getOffset();
		long expectedEndstreamOffset = start + streamLength;
		if (expectedEndstreamOffset > getSource().getStreamLength()) {
			validLength = false;
			LOGGER.log(Level.WARNING, getErrorMessage("Couldn't find expected endstream keyword", expectedEndstreamOffset));
		} else {
			getSource().seek(expectedEndstreamOffset);

			getBaseParser().nextToken();
			final Token token = getBaseParser().getToken();
			if (token.type != Token.Type.TT_KEYWORD ||
					token.keyword != Token.Keyword.KW_ENDSTREAM) {
				validLength = false;
				LOGGER.log(Level.WARNING, getErrorMessage("Couldn't find expected endstream keyword", expectedEndstreamOffset));
			}

			getSource().seek(start);
		}
		return validLength;
	}

	private void checkEndstreamSpacings(COSObject stream, long streamStartOffset, Long expectedLength) throws IOException {
		getBaseParser().skipSpaces();

		long approximateLength = getSource().getOffset() - streamStartOffset;
		long expected = expectedLength == null ? 0 : expectedLength;

		getSource().unread(2);
		int firstSymbol = getSource().readByte();
		int secondSymbol = getSource().readByte();
		byte eolCount = 0;
		if (secondSymbol == 10) {
			if (firstSymbol == 13) {
				long diff = approximateLength - expected;
				eolCount = (byte) (diff > 1 ? 2 : 1);
			} else {
				eolCount = 1;
			}
		} else if (secondSymbol == 13) {
			eolCount = 1;
		} else {
			LOGGER.log(Level.FINE, getErrorMessage("End of stream doesn't contain EOL marker"));
			stream.setEndstreamKeywordCRLFCompliant(false);
		}

		stream.setRealStreamSize(approximateLength - eolCount);
		getBaseParser().nextToken();
	}

	public COSDocument getDocument() {
		return document;
	}

	@Override
	protected String getErrorMessage(String message) {
		return getErrorMessage(message, getSource().getCurrentOffset());
	}

	protected String getErrorMessage(String message, long offset) {
		if (keyOfCurrentObject != null) {
			return message + "(object key = " + keyOfCurrentObject + ", offset = " + offset + ')';
		}
		return getBaseParser().getErrorMessage(message, offset);
	}

	@Override
	public SeekableBaseParser getBaseParser() {
		return (SeekableBaseParser) super.getBaseParser();
	}

	@Override
	public SeekableInputStream getSource() {
		return getBaseParser().getSource();
	}

	public void closeInputStream() throws IOException {
		getBaseParser().closeInputStream();
	}
}
