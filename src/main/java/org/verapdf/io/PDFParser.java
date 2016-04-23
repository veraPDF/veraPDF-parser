package org.verapdf.io;

import org.apache.log4j.Logger;
import org.verapdf.as.ASAtom;
import org.verapdf.as.exceptions.StringExceptions;
import org.verapdf.as.io.ASInputStream;
import org.verapdf.cos.*;
import org.verapdf.cos.xref.COSXRefEntry;
import org.verapdf.cos.xref.COSXRefInfo;
import org.verapdf.cos.xref.COSXRefSection;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * @author Timur Kamalov
 */
public class PDFParser extends Parser {

    private static final Logger LOG = Logger.getLogger(PDFParser.class);

    private static final String HEADER_PATTERN = "%PDF-";
    private static final String PDF_DEFAULT_VERSION = "1.4";

    private COSDocument document;
    private Queue<COSObject> objects;
    private Queue<Integer> integers;
    private boolean flag;

    private static final byte XREF_SEARCH_INC = 32;
    private static final byte XREF_SEARCH_STEP_MAX = 32;

    public PDFParser(final String filename) throws Exception {
        super(filename);
        this.document = new COSDocument(null);
        this.objects = new LinkedList<COSObject>();
        this.integers = new LinkedList<Integer>();
        this.flag = true;
    }

    public PDFParser(final COSDocument document, final String filename) throws Exception { //tmp ??
        this(filename);
        this.document = document;
    }

    public COSHeader getHeader() throws IOException {
        return parseHeader();
    }

    private COSHeader parseHeader() throws IOException {
        COSHeader result = new COSHeader();

        String header = getLine(0);
        if (!header.contains(HEADER_PATTERN))
        {
            header = getLine();
            while (!header.contains(HEADER_PATTERN) && !header.contains(HEADER_PATTERN.substring(1))) {
                if ((header.length() > 0) && (Character.isDigit(header.charAt(0)))) {
                    break;
                }
                header = getLine();
            }
        }

        do {
            unread();
        } while (isNextByteEOL());
        readByte();

        final int headerStart = header.indexOf(HEADER_PATTERN);
        final long headerOffset = getOffset() - header.length() + headerStart;

        result.setHeaderOffset(headerOffset);
        result.setHeader(header);

        skipSpaces(false);

        if (headerStart > 0) {
            //trim off any leading characters
            header = header.substring(headerStart, header.length());
        }

        // This is used if there is garbage after the header on the same line
        if (header.startsWith(HEADER_PATTERN) && !header.matches(HEADER_PATTERN + "\\d.\\d"))
        {
            if (header.length() < HEADER_PATTERN.length() + 3) {
                // No version number at all, set to 1.4 as default
                header = HEADER_PATTERN + PDF_DEFAULT_VERSION;
                LOG.warn("No version found, set to " + PDF_DEFAULT_VERSION + " as default.");
            } else {
                // trying to parse header version if it has some garbage
                Integer pos = null;
                if (header.indexOf(37) > -1) {
                    pos = Integer.valueOf(header.indexOf(37));
                } else if (header.contains("PDF-")) {
                    pos = Integer.valueOf(header.indexOf("PDF-"));
                }
                if (pos != null) {
                    Integer length = Math.min(8, header.substring(pos).length());
                    header = header.substring(pos, pos + length);
                }
            }
        }
        float headerVersion = 1.4f;
        try {
            String[] headerParts = header.split("-");
            if (headerParts.length == 2) {
                headerVersion = Float.parseFloat(headerParts[1]);
            }
        }
        catch (NumberFormatException e) {
            LOG.warn("Can't parse the header version.", e);
        }

        result.setVersion(headerVersion);
        checkComment(result);

        // rewind
        seek(0);
        return result;
    }

    /** check second line of pdf header
     */
    private void checkComment(final COSHeader header) throws IOException {
        String comment = getLine();
        boolean isValidComment = true;

        if (comment != null && !comment.isEmpty()) {
            if (comment.charAt(0) != '%') {
                isValidComment = false;
            }

            int pos = comment.indexOf('%') > -1 ? comment.indexOf('%') + 1 : 0;
            if (comment.substring(pos).length() < 4) {
                isValidComment = false;
            }
        } else {
            isValidComment = false;
        }
        if (isValidComment) {
            header.setBinaryHeaderBytes(comment.charAt(1), comment.charAt(2),
                                        comment.charAt(3), comment.charAt(4));
        } else {
            header.setBinaryHeaderBytes(-1, -1, -1, -1);
        }
    }

    public void getXRefInfo(List<COSXRefInfo> infos) throws Exception {
        this.getXRefInfo(infos, 0);
    }

    public COSObject nextObject() throws IOException {
        if (!this.objects.isEmpty()) {
            COSObject result = this.objects.peek();
            this.objects.remove();
            return result;
        }

        if (this.flag) {
            nextToken();
        }
        this.flag = true;

        final Token token = getToken();

        if (token.type == Token.Type.TT_INTEGER) {  // looking for indirect reference
            this.integers.add((int) token.integer);
            if (this.integers.size() == 3) {
                COSObject result = COSInteger.construct(this.integers.peek());
                this.integers.remove();
                return result;
            }
            return nextObject();
        }

        if (token.type == Token.Type.TT_KEYWORD
                && token.keyword == Token.Keyword.KW_R
                && this.integers.size() == 2) {
            final int number = this.integers.peek();
            this.integers.remove();
            final int generation = this.integers.peek();
            this.integers.remove();
            return COSIndirect.construct(new COSKey(number, generation), document);
        }

        if (!this.integers.isEmpty()) {
            COSObject result = COSInteger.construct(this.integers.peek());
            this.integers.remove();
            while (!this.integers.isEmpty()) {
                this.objects.add(COSInteger.construct(this.integers.peek()));
                this.integers.remove();
            }
            this.flag = false;
            return result;
        }

        switch (token.type) {
            case TT_NONE:
                break;
            case TT_KEYWORD: {
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
                return COSString.construct(token.token);
            case TT_HEXSTRING:
                return COSString.construct(token.token, true);
            case TT_NAME:
                return COSName.construct(token.token);
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

	public COSObject getObject(final long offset) throws IOException {
		clear();

		seek(offset);

		final Token token = getToken();

		nextToken();
		if (token.type != Token.Type.TT_INTEGER) {
			return new COSObject();
		}
		long number = token.integer;

		nextToken();
		if (token.type != Token.Type.TT_INTEGER) {
			return new COSObject();
		}
		long generation = token.integer;

		nextToken();
		if (token.type != Token.Type.TT_KEYWORD &&
				token.keyword != Token.Keyword.KW_OBJ) {
			return new COSObject();
		}

		COSObject obj = nextObject();

		if (this.flag) {
			nextToken();
		}
		this.flag = true;

		if (token.type != Token.Type.TT_KEYWORD &&
				token.keyword != Token.Keyword.KW_ENDOBJ) {
			closeInputStream();
			// TODO : replace with ASException
			throw new IOException("PDFParser::GetObject(...)" + StringExceptions.INVALID_PDF_OBJECT);
		}

		return obj;
	}

	private void clear() {
		this.objects.clear();
		this.integers.clear();
		this.flag = true;
	}

    private long findLastXRef() throws IOException {
        seekFromEnd(30);
		if (findKeyword(Token.Keyword.KW_STARTXREF)) {
			nextToken();
			if (getToken().type == Token.Type.TT_INTEGER) {
				return getToken().integer;
			}
		}
		return 0;
    }

	private void getXRefSection(final COSXRefSection xrefs) throws IOException {
		nextToken();
		if (getToken().type != Token.Type.TT_KEYWORD || getToken().keyword != Token.Keyword.KW_XREF) {
			closeInputStream();
			throw new IOException("PDFParser::GetXRefSection(...)" + StringExceptions.CAN_NOT_LOCATE_XREF_TABLE);
		}

		nextToken();
		while (getToken().type == Token.Type.TT_INTEGER) {
			int number = (int) getToken().integer;
			nextToken();
			int count = (int) getToken().integer;
			COSXRefEntry xref;
			for (int i = 0; i < count; ++i) {
                xref = new COSXRefEntry();
				nextToken();
				xref.offset = getToken().integer;
				nextToken();
				xref.generation = (int) getToken().integer;
				nextToken();
				xref.free = getToken().token.charAt(0);
				xrefs.addEntry(number + i, xref);
			}
			nextToken();
		}
		seekFromCurrentPosition(-7);
	}

	private void getXRefInfo(final List<COSXRefInfo> info, long offset) throws Exception {
		if (offset == 0) {
			offset = findLastXRef();
			if (offset == 0) {
				closeInputStream();
				throw new IOException("PDFParser::GetXRefInfo(...)" + StringExceptions.START_XREF_VALIDATION);
			}
		}
		clear();
		seek(offset);

		COSXRefInfo section = new COSXRefInfo();
		info.add(0, section);

		section.setStartXRef(offset);
		getXRefSection(section.getXRefSection());
		getTrailer(section.getTrailer());

		offset = section.getTrailer().getPrev();
		if (offset == 0) {
			return;
		}

		getXRefInfo(info, offset);
	}

	private void getTrailer(final COSTrailer trailer) throws Exception {
		if (findKeyword(Token.Keyword.KW_TRAILER)) {
			COSObject obj = nextObject();
			trailer.setObject(obj);
		}

		if (trailer.knownKey(ASAtom.ENCRYPT)) {
			closeInputStream();
			throw new Exception("PDFParser::GetTrailer(...)" + StringExceptions.ENCRYPTED_PDF_NOT_SUPPORTED);
		}

        if (trailer.knownKey(ASAtom.XREF_STM)) {
            closeInputStream();
            throw new Exception("PDFParser::GetTrailer(...)" + StringExceptions.XREF_STM_NOT_SUPPORTED);
        }
	}

    private COSObject getArray() throws IOException {
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
            closeInputStream();
            // TODO : replace with ASException
            throw new IOException("PDFParser::GetArray()" + StringExceptions.INVALID_PDF_ARRAY);
        }

        return arr;
    }

    private COSObject getName() throws IOException {
        if (this.flag) {
            nextToken();
        }
        this.flag = true;

        final Token token = getToken();
        if (token.type != Token.Type.TT_NAME) {
            return new COSObject();
        }
        return COSName.construct(token.token);
    }

    private COSObject getDictionary() throws IOException {
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
            closeInputStream();
            // TODO : replace with ASException
            throw new IOException("PDFParser::GetDictionary()" + StringExceptions.INVALID_PDF_DICTONARY);
        }

        if (this.flag) {
            nextToken();
        }
        this.flag = false;

        if (token.type == Token.Type.TT_KEYWORD &&
                token.keyword == Token.Keyword.KW_STREAM) {
            return getStream(dict);
        }

        return dict;
    }

	private COSObject getStream(COSObject dict) throws IOException {
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
        long streamStartOffset = getOffset();

        long size = dict.getKey(ASAtom.LENGTH).getInteger();
        seek(streamStartOffset);

        boolean streamLengthValid = checkStreamLength(size);

        if (streamLengthValid) {
            dict.setRealStreamSize(size);
            ASInputStream stm = super.getStream(size);
            dict.setData(stm);
        } else {
            //trying to find endstream keyword
            long realStreamSize = -1;
            int bufferLength = (int) (size > 512 ? 512 : size);
            byte[] buffer = new byte[bufferLength];
            while (!isEof()) {
                int bytesRead = read(buffer, bufferLength);
                for (int i = 0; i < bytesRead; i++) {
                    if (buffer[i] == 101) {
                        long reset = getOffset();
                        long possibleEndstreamOffset = reset - bytesRead + i;
                        seek(possibleEndstreamOffset);
                        nextToken();
                        if (token.type == Token.Type.TT_KEYWORD &&
                                token.keyword == Token.Keyword.KW_ENDSTREAM) {
                            realStreamSize = possibleEndstreamOffset - streamStartOffset;
                            dict.setRealStreamSize(realStreamSize);
                            seek(possibleEndstreamOffset);
                            break;
                        }
                        seek(reset);
                    }
                }
                if (realStreamSize != -1) {
                    break;
                }
            }
            if (realStreamSize == -1) {
                //TODO : exception?
            }
        }

        checkEndstreamSpacings(dict, size);

		return dict;
	}

    private void checkStreamSpacings(COSObject stream) throws IOException {
        byte whiteSpace = readByte();
        if (whiteSpace == 13) {
            whiteSpace = readByte();
            if (whiteSpace != 10) {
                stream.setStreamKeywordCRLFCompliant(Boolean.FALSE);
                unread();
            }
        } else if (whiteSpace != 10) {
            LOG.warn("Stream at " + getOffset() + " offset has no EOL marker.");
            stream.setStreamKeywordCRLFCompliant(Boolean.FALSE);
            unread();
        }
    }

    private boolean checkStreamLength(long streamLength) throws IOException {
        boolean validLength = true;
        long start = getOffset();
        long expectedEndstreamOffset = start + streamLength;
        if (expectedEndstreamOffset > getSourceLength()) {
            validLength = false;
            LOG.warn("Couldn't find expected endstream keyword at offset " + expectedEndstreamOffset);
        } else {
            seek(expectedEndstreamOffset);

            nextToken();
            final Token token = getToken();
            if (token.type != Token.Type.TT_KEYWORD ||
                    token.keyword != Token.Keyword.KW_ENDSTREAM) {
                validLength = false;
                LOG.warn("Couldn't find expected endstream keyword at offset " + expectedEndstreamOffset);
            }

            seek(start);
        }
        return validLength;
    }

    private void checkEndstreamSpacings(COSObject stream, long expectedLength) throws IOException {
        byte eolCount = 0;

        long diff = stream.getRealStreamSize() - expectedLength;

        unread(2);
        int firstSymbol = readByte();
        int secondSymbol = readByte();
        if (secondSymbol == 10) {
            if (firstSymbol == 13) {
                eolCount = (byte) (diff == 1 ? 1 : 2);
            } else {
                eolCount = 1;
            }
        } else if (secondSymbol == 13) {
            eolCount = 1;
        } else {
            LOG.warn("End of stream at " + getOffset() + " offset doesn't contain EOL marker.");
            stream.setEndstreamKeywordCRLFCompliant(false);
        }

        stream.setRealStreamSize(stream.getRealStreamSize() - eolCount);
    }

}
