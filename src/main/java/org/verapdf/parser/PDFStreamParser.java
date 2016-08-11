package org.verapdf.parser;

import org.verapdf.as.CharTable;
import org.verapdf.cos.*;
import org.verapdf.operator.Operator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * @author Timur Kamalov
 */
public class PDFStreamParser extends COSParser {

	private final List<Object> tokens = new ArrayList<>();

	public PDFStreamParser(COSStream stream) throws IOException {
		super(stream.getData());
		initializeToken();
	}

	public void parseTokens() throws IOException {
		Object token = parseNextToken();
		while (token != null) {
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

		skipSpaces();
		byte nextByte = source.peek();
		if (nextByte == -1) {
			return null;
		}

		byte c = nextByte;

		switch (c) {
			case '<': {
				//check brackets
				source.readByte();
				c = source.peek();
				source.unread();

				if (c == '<') {
					result = getDictionary();
				} else {
					nextToken();
					result = COSString.construct(getToken().getValue());
				}
				break;
			}
			case '[': {
				result = getArray();
				break;
			}
			case '(':
				nextToken();
				result = COSString.construct(getToken().getValue());
				break;
			case '/':
				// name
				result = getName();
				break;
			case 'n': {
				// null
				String nullString = readUntilWhitespace();
				if (nullString.equals("null")) {
					result = COSNull.NULL;
				} else {
					result = Operator.getOperator(nullString);
				}
				break;
			}
			case 't':
			case 'f': {
				String line = readUntilWhitespace();
				if (line.equals("true")) {
					result = COSBoolean.TRUE;
					break;
				} else if (line.equals("false")) {
					result = COSBoolean.FALSE;
				} else {
					result = Operator.getOperator(line);
				}
				break;
			}
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
			//TODO : Support inline image operators
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
		byte nextByte = source.peek();
		while (nextByte != -1 && // EOF
				!CharTable.isSpace(nextByte) && nextByte != ']' &&
				nextByte != '[' && nextByte != '<' &&
				nextByte != '(' && nextByte != '/' &&
				(nextByte < '0' || nextByte > '9'))	{
			byte currentByte = source.readByte();
			nextByte = source.peek();
			buffer.append((char) currentByte);
			// d0 and d1 operators
			if (currentByte == 'd' && (nextByte == '0' || nextByte == '1') ) {
				buffer.append((char) source.readByte());
				nextByte = source.peek();
			}
		}

		return buffer.toString();
	}

}
