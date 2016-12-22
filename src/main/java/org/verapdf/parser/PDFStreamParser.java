package org.verapdf.parser;

import org.verapdf.as.CharTable;
import org.verapdf.cos.*;
import org.verapdf.operator.InlineImageOperator;
import org.verapdf.operator.Operator;

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
public class PDFStreamParser extends COSParser {

	private static final Logger LOGGER = Logger.getLogger(PDFStreamParser.class.getCanonicalName());

	private final List<Object> tokens = new ArrayList<>();

	public PDFStreamParser(COSStream stream) throws IOException {
		super(stream.getData(COSStream.FilterFlags.DECODE));
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
		byte nextByte = (byte) source.peek();
		if (nextByte == -1) {
			return null;
		}

		byte c = nextByte;

		switch (c) {
			case '<': {
				//check brackets
				source.readByte();
				c = (byte) source.peek();
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
			case '(':
				nextToken();
				result = COSString.construct(getToken().getByteValue());
				break;
			case '/':
				// name
				result = getName();
				break;
			case 'n': {
				// null
				String nullString = readUntilWhitespace();
				if (nullString.equals("null")) {
					result = new COSObject(COSNull.NULL);
				} else {
					result = Operator.getOperator(nullString);
				}
				break;
			}
			case 't':
			case 'f': {
				String line = readUntilWhitespace();
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
				if (source.readByte() != 73 && source.readByte() != 68) {
					//TODO : change
					throw new IOException("Corrupted inline image operator");
				}
				if (CharTable.isSpace(source.peek())) {
					source.readByte();
				}
				long startOffset = source.getOffset();
				int imageStreamLength = 2;
				byte previousByte = source.readByte();
				byte currentByte = source.readByte();
				while (!(previousByte == 'E' && currentByte == 'I') && !source.isEOF()) {
					previousByte = currentByte;
					currentByte = source.readByte();
					imageStreamLength++;
				}
				result = Operator.getOperator("ID");
				((InlineImageOperator) result).setImageData(
						source.getStream(startOffset, imageStreamLength));
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
		StringBuffer buffer = new StringBuffer(5);
		byte nextByte = (byte) source.peek();
		while (source.getOffset() < source.getStreamLength() && // EOF
				!CharTable.isSpace(nextByte) && nextByte != ']' &&
				nextByte != '[' && nextByte != '<' &&
				nextByte != '(' && nextByte != '/' &&
				(nextByte < '0' || nextByte > '9'))	{
			byte currentByte = source.readByte();
			buffer.append((char) currentByte);

			if (source.getOffset() < source.getStreamLength()) {
				// d0 and d1 operators
				nextByte = (byte) source.peek();
				if (currentByte == 'd' && (nextByte == '0' || nextByte == '1')) {
					buffer.append((char) source.readByte());
					nextByte = (byte) source.peek();
				}
			}
		}

		return buffer.toString();
	}

}
