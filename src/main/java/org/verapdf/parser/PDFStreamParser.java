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
import org.verapdf.as.CharTable;
import org.verapdf.as.io.ASInputStream;
import org.verapdf.as.io.ASMemoryInStream;
import org.verapdf.cos.*;
import org.verapdf.operator.InlineImageOperator;
import org.verapdf.operator.Operator;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Timur Kamalov
 */
public class PDFStreamParser extends NotSeekableCOSParser {

	private static final Logger LOGGER = Logger.getLogger(PDFStreamParser.class.getCanonicalName());
	private static final int INLINE_IMAGE_BUFFER_SIZE = 8192;

	private final List<Object> tokens = new ArrayList<>();
	private List<Closeable> imageDataStreams = new ArrayList<>();

	private COSDictionary lastInlineImageDict;

	public PDFStreamParser(ASInputStream stream) throws IOException {
		super(stream);
		initializeToken();
	}

	public void parseTokens() throws IOException {
		Object token = parseNextToken();
		while (token != null) {
			if (token instanceof COSObject) {
				token = ((COSObject) token).get();
			}
			tokens.add(token);
			token = parseNextToken();
		}
	}

	public List<Object> getTokens()
	{
		return this.tokens;
	}

	public Iterator<Object> getTokensIterator() {
		return new Iterator<Object>() {

			private Object token;

			private void tryNext() {
				try {
					if (token == null) {
						token = parseNextToken();
					}
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}

			/** {@inheritDoc} */
			@Override
			public boolean hasNext() {
				tryNext();
				return token != null;
			}

			/** {@inheritDoc} */
			@Override
			public Object next() {
				tryNext();
				Object tmp = token;
				if (tmp == null) {
					throw new NoSuchElementException();
				}
				token = null;
				return tmp;
			}

			/** {@inheritDoc} */
			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}
		};
	}

	/**
	 * This will parse the next token in the stream.
	 *
	 * @return The next token in the stream or null if there are no more tokens in the stream.
	 *
	 * @throws IOException If an io error occurs while parsing the stream.
	 */
	public Object parseNextToken() throws IOException {
		Object result = null;

		skipSpaces(true);
		byte nextByte = source.peek();
		if (nextByte == -1) {
			return null;
		}

		byte c = nextByte;

		switch (c) {
			case '(':
                nextToken();
                result = COSString.construct(getToken().getByteValue());
                break;
            case '<': {
				//check brackets
				source.readByte();
				c = source.peek();
				source.unread();

				if (c == '<') {
					result = getDictionary();
				} else {
					nextToken();
					Token token = getToken();
					result = COSString.construct(token.getByteValue(), true, token.getHexCount(), token.isContainsOnlyHex());
				}
				break;
			}
			case '[': {
				result = getArray();
				break;
			}
			case '/':
				// name
				result = getName();
				break;
			case 'n': {
				// null
				String nullString = readUntilDelimiter();
				if (nullString.equals("null")) {
					result = new COSObject(COSNull.NULL);
				} else {
					result = Operator.getOperator(nullString);
				}
				break;
			}
			case 't':
			case 'f': {
				String line = readUntilDelimiter();
				if (line.equals("true")) {
					result = new COSObject(COSBoolean.TRUE);
					break;
				} else if (line.equals("false")) {
					result = new COSObject(COSBoolean.FALSE);
				} else {
					result = Operator.getOperator(line);
				}
				break;
			}
			case '.':
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
			case '-': {
				Token token = getToken();
				nextToken();
				if (token.type.equals(Token.Type.TT_REAL)) {
					result = COSReal.construct(token.real);
				} else if (token.type.equals(Token.Type.TT_INTEGER)) {
					result = COSInteger.construct(token.integer);
				}
				break;
			}
			// BI operator
			case 'B': {
				Token token = getToken();
				nextToken();
				result = Operator.getOperator(token.getValue());
				if (result instanceof InlineImageOperator) {
					InlineImageOperator imageOperator = (InlineImageOperator) result;
					COSDictionary imageParameters = (COSDictionary) COSDictionary.construct().get();
					lastInlineImageDict = imageParameters;
					imageOperator.setImageParameters(imageParameters);
					Object nextToken = parseNextToken();
					while (nextToken instanceof COSObject &&
							((COSObject) nextToken).getType() == COSObjType.COS_NAME) {
						Object value = parseNextToken();
						if (value instanceof COSObject) {
							imageParameters.setKey(((COSObject) nextToken).getName(), (COSObject) value);
						} else {
							LOGGER.log(Level.FINE, "Unexpected token in BI operator parsing: " + value.toString());
						}
						nextToken = parseNextToken();
					}

					if (nextToken instanceof InlineImageOperator) {
						imageOperator.setImageData(((InlineImageOperator) nextToken).getImageData());
					} else {
						throw new IOException("Unexpected token instead of " +
								"operator in operator parsing: " + nextToken.toString());
					}
				}
				break;
			}
			// ID operator
			case 'I': {
				//looking for an ID operator
				if (source.readByte() != 'I' && source.readByte() != 'D') {
					//TODO : change
					throw new IOException("Corrupted inline image operator");
				}
				if (CharTable.isSpace(source.peek())) {
					source.readByte();
				}
				ASInputStream imageDataStream = readInlineImage();
				result = Operator.getOperator("ID");
				this.imageDataStreams.add(imageDataStream);
				((InlineImageOperator) result).setImageData(imageDataStream);
				break;
			}
			default: {
				String operator = nextOperator();
				if (operator.length() == 0) {
					//stream is corrupted
					result = null;
				} else {
					result = Operator.getOperator(operator);
				}
			}
		}
		return result;
	}

	protected String nextOperator() throws IOException {
		skipSpaces();

		//maximum possible length of an operator is 3 and we'll leave some space for invalid cases
		StringBuilder buffer = new StringBuilder(5);
		byte nextByte = source.peek();
		while (!source.isEOF() &&
				!CharTable.isSpace(nextByte) && nextByte != ']' &&
				nextByte != '[' && nextByte != '<' &&
				nextByte != '(' && nextByte != '/' &&
				(nextByte < '0' || nextByte > '9'))	{
			byte currentByte = source.readByte();
			buffer.append((char) currentByte);

			if (!source.isEOF()) {
				// d0 and d1 operators
				nextByte = source.peek();
				if (currentByte == 'd' && (nextByte == '0' || nextByte == '1')) {
					buffer.append((char) source.readByte());
					nextByte = source.peek();
				}
			}
		}

		return buffer.toString();
	}

	private ASInputStream readInlineImage() throws IOException {
		source.resetReadCounter();
		ArrayList<Byte> image = new ArrayList<>(INLINE_IMAGE_BUFFER_SIZE);
		byte previousByte = source.readByte();
		byte currentByte = source.readByte();
		image.add(previousByte);
		image.add(currentByte);
		Long l = this.lastInlineImageDict == null ? new Long(0) : lastInlineImageDict.getIntegerKey(ASAtom.L);
		if (l == null) {
			l = lastInlineImageDict.getIntegerKey(ASAtom.LENGTH);
		}
		while (!(this.source.isEOF())) {
			if (previousByte == 'E' && currentByte == 'I' && isSourceAfterImage(l) && CharTable.isSpace(source.peek())) {
			    break;
            }
			previousByte = currentByte;
			currentByte = source.readByte();
			image.add(currentByte);
		}
		return new ASMemoryInStream(getByteArrayFromArrayList(image),
				source.getReadCounter(), false);
	}

	private boolean isSourceAfterImage(Long length) {
		 return length == null || source.getReadCounter() >= length;
	}

	public List<Closeable> getImageDataStreams() {
		return imageDataStreams;
	}

	public static byte[] getByteArrayFromArrayList(ArrayList<Byte> list) {
		byte[] res = new byte[list.size()];
		int i = 0;
		for (Byte b : list) {
			res[i++] = b;
		}
		return res;
	}
}
