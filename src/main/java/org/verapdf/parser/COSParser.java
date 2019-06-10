/**
 * This file is part of veraPDF Parser, a module of the veraPDF project.
 * Copyright (c) 2015, veraPDF Consortium <info@verapdf.org>
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
import org.verapdf.as.exceptions.StringExceptions;
import org.verapdf.as.io.ASInputStream;
import org.verapdf.cos.*;
import org.verapdf.io.InternalInputStream;
import org.verapdf.io.SeekableInputStream;
import org.verapdf.pd.encryption.StandardSecurityHandler;
import org.verapdf.tools.resource.ASFileStreamCloser;

import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.util.LinkedList;
import java.util.Queue;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Timur Kamalov
 */
public class COSParser extends BaseParser {

	private static final Logger LOGGER = Logger.getLogger(COSParser.class.getCanonicalName());

	/**
	 * Linearization dictionary must be in first 1024 bytes of document
	 */
	protected final int LINEARIZATION_DICTIONARY_LOOKUP_SIZE = 1024;

	protected COSDocument document;
	protected Queue<COSObject> objects = new LinkedList<>();
	protected Queue<Long> integers = new LinkedList<>();
    protected COSKey keyOfCurrentObject;

	protected boolean flag = true;

	public COSParser(final SeekableInputStream seekableInputStream) throws IOException {
		super(seekableInputStream);
	}

	public COSParser(final String filename) throws IOException {
		super(filename);
	}

	public COSParser(final InputStream fileStream) throws IOException {
		super(fileStream);
	}

	public COSParser(final COSDocument document, final String filename) throws IOException { //tmp ??
		this(filename);
		this.document = document;
	}

	public COSParser(final COSDocument document, final InputStream fileStream) throws IOException { //tmp ??
		this(fileStream);
		this.document = document;
	}

	public COSObject nextObject() throws IOException {
		if (!this.objects.isEmpty()) {
			COSObject result = this.objects.peek();
			this.objects.remove();
			return result;
		}

		if (this.flag) {
			initializeToken();
			nextToken();
		}
		this.flag = true;

		final Token token = getToken();

		if (token.type == Token.Type.TT_INTEGER) {  // looking for indirect reference
			this.integers.add(Long.valueOf(token.integer));
			if (this.integers.size() == 3) {
				COSObject result = COSInteger.construct(this.integers.peek().longValue());
				this.integers.remove();
				return result;
			}
			return nextObject();
		}

		if (token.type == Token.Type.TT_KEYWORD
				&& token.keyword == Token.Keyword.KW_R
				&& this.integers.size() == 2) {
			final int number = this.integers.peek().intValue();
			this.integers.remove();
			final int generation = this.integers.peek().intValue();
			this.integers.remove();
			return COSIndirect.construct(new COSKey(number, generation), document);
		}

		if (!this.integers.isEmpty()) {
			COSObject result = COSInteger.construct(this.integers.peek().longValue());
			this.integers.remove();
			while (!this.integers.isEmpty()) {
				this.objects.add(COSInteger.construct(this.integers.peek().longValue()));
				this.integers.remove();
			}
			this.flag = false;
			return result;
		}

		switch (token.type) {
			case TT_NONE:
				break;
			case TT_KEYWORD: {
				if (token.keyword == null) {
					break;
				}
				switch (token.keyword) {
					case KW_NONE:
						break;
					case KW_NULL:
						return COSNull.construct();
					case KW_TRUE:
						return COSBoolean.construct(true);
					case KW_FALSE:
						return COSBoolean.construct(false);
					case KW_STREAM:
					case KW_ENDSTREAM:
					case KW_OBJ:
					case KW_ENDOBJ:
					case KW_R:
					case KW_N:
					case KW_F:
					case KW_XREF:
					case KW_STARTXREF:
					case KW_TRAILER:
						break;
				}
				break;
			}
			case TT_INTEGER: //should not enter here
				break;
			case TT_REAL:
				return COSReal.construct(token.real);
			case TT_LITSTRING:
				return COSString.construct(token.getByteValue());
			case TT_HEXSTRING:
				COSObject res = COSString.construct(token.getByteValue(), true,
						token.getHexCount().longValue(), token.isContainsOnlyHex());
				if(this.document == null || !this.document.isEncrypted()) {
					return res;
				}
			return this.decryptCOSString(res);
			case TT_NAME:
				return COSName.construct(token.getValue());
			case TT_OPENARRAY:
				this.flag = false;
				return getArray();
			case TT_CLOSEARRAY:
				return new COSObject();
			case TT_OPENDICT:
				this.flag = false;
				return getDictionary();
			case TT_CLOSEDICT:
				return new COSObject();
			case TT_EOF:
				return new COSObject();
		}
		return new COSObject();
	}

	protected COSObject getArray() throws IOException {
		if (this.flag) {
			nextToken();
		}
		this.flag = true;

		final Token token = getToken();
		if (token.type != Token.Type.TT_OPENARRAY) {
			return new COSObject();
		}

		COSObject arr = COSArray.construct();

		COSObject obj = nextObject();
		while(!obj.empty()) {
			arr.add(obj);
			obj = nextObject();
		}

		if (token.type != Token.Type.TT_CLOSEARRAY) {
			// TODO : replace with ASException
			throw new IOException("PDFParser::GetArray()" + StringExceptions.INVALID_PDF_ARRAY);
		}

		return arr;
	}

	protected COSObject getName() throws IOException {
		if (this.flag) {
			nextToken();
		}
		this.flag = true;

		final Token token = getToken();
		if (token.type != Token.Type.TT_NAME) {
			return new COSObject();
		}
		return COSName.construct(token.getValue());
	}

	protected COSObject getDictionary() throws IOException {
		if (this.flag) {
			nextToken();
		}
		this.flag = true;
		final Token token = getToken();

		if (token.type != Token.Type.TT_OPENDICT) {
			return new COSObject();
		}

		COSObject dict = COSDictionary.construct();

		COSObject key = getName();
		while (!key.empty()) {
			COSObject obj = nextObject();
			dict.setKey(key.getName(), obj);
			key = getName();
		}

		if (token.type != Token.Type.TT_CLOSEDICT) {
			// TODO : replace with ASException
			throw new IOException("PDFParser::GetDictionary()" + StringExceptions.INVALID_PDF_DICTONARY);
		}

		long reset = this.source.getOffset();
		if (this.flag) {
			nextToken();
		}
		this.flag = false;

		if (token.type == Token.Type.TT_KEYWORD &&
				token.keyword == Token.Keyword.KW_STREAM) {
			return getStream(dict);
		}
		this.source.seek(reset);
		this.flag = true;

		return dict;
	}

	protected COSObject getStream(COSObject dict) throws IOException {
		if (this.flag) {
			nextToken();
		}
		this.flag = true;

		final Token token = getToken();

		if (token.type != Token.Type.TT_KEYWORD ||
				token.keyword != Token.Keyword.KW_STREAM) {
			this.flag = false;
			return dict;
		}

		checkStreamSpacings(dict);
		long streamStartOffset = source.getOffset();

		Long size = dict.getKey(ASAtom.LENGTH).getInteger();
		source.seek(streamStartOffset);

		boolean streamLengthValid = checkStreamLength(size);

		if (streamLengthValid) {
			dict.setRealStreamSize(size);
			ASInputStream stm = super.getRandomAccess(size);
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
			while (realStreamSize == -1 && !source.isEOF()) {
				long bytesRead = source.read(buffer, bufferLength);
				for (int i = 0; i < bytesRead; i++) {
					if (buffer[i] == 101) {
						long reset = source.getOffset();
						long possibleEndStreamOffset = reset - bytesRead + i - eolLength;
						source.seek(possibleEndStreamOffset);
						nextToken();
						if (token.type == Token.Type.TT_KEYWORD &&
								token.keyword == Token.Keyword.KW_ENDSTREAM) {
							realStreamSize = possibleEndStreamOffset - streamStartOffset;
							dict.setRealStreamSize(realStreamSize);
							source.seek(streamStartOffset);
							ASInputStream stm = super.getRandomAccess(realStreamSize);
							dict.setData(stm);
							source.seek(possibleEndStreamOffset);
							if (stm instanceof InternalInputStream) {
								this.document.addFileResource(new ASFileStreamCloser(stm));
							}
							break;
						}
						source.seek(reset);
					}

					//we need to subtract eol before endstream length from stream length
					if (isCR(buffer[i])) {
						// if current byte is CR, then this is the 1st byte of eol
						eolLength = 1;
						isPrevCR = true;
					} else {
						if (isLF(buffer[i])) {
							eolLength = isPrevCR ? 2 : 1;
						} else {
							eolLength = 0;
						}
						isPrevCR = false;
					}
				}
			}
			if (realStreamSize == -1) {
				//TODO : exception?
			}
		}

		checkEndstreamSpacings(dict, streamStartOffset, size);

		return dict;
	}


	private void checkStreamSpacings(COSObject stream) throws IOException {
		byte whiteSpace = source.readByte();
		if (isCR(whiteSpace)) {
			whiteSpace = source.readByte();
			if (!isLF(whiteSpace)) {
				stream.setStreamKeywordCRLFCompliant(false);
				source.unread();
			}
		} else if (!isLF(whiteSpace)) {
			LOGGER.log(Level.WARNING, "Stream at " + source.getOffset() + " offset has no EOL marker.");
			stream.setStreamKeywordCRLFCompliant(false);
			source.unread();
		}
	}

	private boolean checkStreamLength(Long streamLength) throws IOException {
		if (streamLength == null) {
			LOGGER.log(Level.WARNING, "Stream length is missing");
			return false;
		}
		boolean validLength = true;
		long start = source.getOffset();
		long expectedEndstreamOffset = start + streamLength;
		if (expectedEndstreamOffset > source.getStreamLength()) {
			validLength = false;
			LOGGER.log(Level.WARNING, "Couldn't find expected endstream keyword at offset " + expectedEndstreamOffset);
		} else {
			source.seek(expectedEndstreamOffset);

			nextToken();
			final Token token = getToken();
			if (token.type != Token.Type.TT_KEYWORD ||
					token.keyword != Token.Keyword.KW_ENDSTREAM) {
				validLength = false;
				LOGGER.log(Level.WARNING, "Couldn't find expected endstream keyword at offset " + expectedEndstreamOffset);
			}

			source.seek(start);
		}
		return validLength;
	}

	private void checkEndstreamSpacings(COSObject stream, long streamStartOffset, Long expectedLength) throws IOException {
		skipSpaces();

		byte eolCount = 0;
		long approximateLength = source.getOffset() - streamStartOffset;
		long expected = expectedLength == null ? 0 : expectedLength;
		long diff = approximateLength - expected;

		source.unread(2);
		int firstSymbol = source.readByte();
		int secondSymbol = source.readByte();
		if (secondSymbol == 10) {
			if (firstSymbol == 13) {
				eolCount = (byte) (diff > 1 ? 2 : 1);
			} else {
				eolCount = 1;
			}
		} else if (secondSymbol == 13) {
			eolCount = 1;
		} else {
			LOGGER.log(Level.FINE, "End of stream at " + source.getOffset() + " offset doesn't contain EOL marker.");
			stream.setEndstreamKeywordCRLFCompliant(false);
		}

		stream.setRealStreamSize(approximateLength - eolCount);
		nextToken();
	}

	public COSDocument getDocument() {
		return document;
	}

	private COSObject decryptCOSString(COSObject string) {
		StandardSecurityHandler ssh =
				this.document.getStandardSecurityHandler();
        try {
            ssh.decryptString((COSString) string.getDirectBase(), this.keyOfCurrentObject);
            return string;
        } catch (IOException | GeneralSecurityException e) {
            LOGGER.log(Level.WARNING, "Can't decrypt string in object " + this.keyOfCurrentObject);
            return string;
        }
	}
}
