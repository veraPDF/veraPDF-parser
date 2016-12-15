package org.verapdf.parser;

import org.verapdf.as.ASAtom;
import org.verapdf.as.CharTable;
import org.verapdf.as.exceptions.StringExceptions;
import org.verapdf.cos.*;
import org.verapdf.cos.xref.COSXRefEntry;
import org.verapdf.cos.xref.COSXRefInfo;
import org.verapdf.cos.xref.COSXRefSection;
import org.verapdf.io.SeekableInputStream;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Timur Kamalov
 */
public class PDFParser extends COSParser {

    private static final Logger LOGGER = Logger.getLogger(PDFParser.class.getCanonicalName());

    private static final String HEADER_PATTERN = "%PDF-";
    private static final String PDF_DEFAULT_VERSION = "1.4";
    private static final byte[] STARTXREF = "startxref".getBytes();

    //%%EOF marker byte representation
    private static final byte[] EOF_MARKER = new byte[]{37, 37, 69, 79, 70};

    private long offsetShift = 0;
    private boolean isEncrypted;
    private COSObject encryption;
    private COSObject id;
    private Long lastTrailerOffset = 0L;

    public PDFParser(final String filename) throws IOException {
        super(filename);
    }

    public PDFParser(final InputStream fileStream) throws IOException {
        super(fileStream);
    }

    public PDFParser(final COSDocument document, final String filename) throws IOException { //tmp ??
        super(document, filename);
    }

    public PDFParser(final COSDocument document, final InputStream fileStream) throws IOException { //tmp ??
        super(document, fileStream);
    }

    public COSHeader getHeader() throws IOException {
        return parseHeader();
    }

    public SeekableInputStream getPDFSource() {
        return this.source;
    }

    private COSHeader parseHeader() throws IOException {
        COSHeader result = new COSHeader();

        String header = getLine(0);
        if (!header.contains(HEADER_PATTERN)) {
            header = getLine();
            while (!header.contains(HEADER_PATTERN) && !header.contains(HEADER_PATTERN.substring(1))) {
                if ((header.length() > 0) && (Character.isDigit(header.charAt(0)))) {
                    break;
                }
                header = getLine();
            }
        }

        do {
            source.unread();
        } while (isNextByteEOL());
        source.readByte();

        final int headerStart = header.indexOf(HEADER_PATTERN);
        final long headerOffset = source.getOffset() - header.length() + headerStart;

        this.offsetShift = headerOffset;
        result.setHeaderOffset(headerOffset);
        result.setHeader(header);

        skipSingleEol();

        if (headerStart > 0) {
            //trim off any leading characters
            header = header.substring(headerStart, header.length());
        }

        // This is used if there is garbage after the header on the same line
        if (header.startsWith(HEADER_PATTERN) && !header.matches(HEADER_PATTERN + "\\d.\\d")) {
            if (header.length() < HEADER_PATTERN.length() + 3) {
                // No version number at all, set to 1.4 as default
                header = HEADER_PATTERN + PDF_DEFAULT_VERSION;
                LOGGER.log(Level.WARNING, "No version found, set to " + PDF_DEFAULT_VERSION + " as default.");
            } else {
                // trying to parse header version if it has some garbage
                Integer pos = null;
                if (header.indexOf(37) > -1) {
                    pos = Integer.valueOf(header.indexOf(37));
                } else if (header.contains("PDF-")) {
                    pos = Integer.valueOf(header.indexOf("PDF-"));
                }
                if (pos != null) {
                    int length = Math.min(8, header.substring(pos.intValue()).length());
                    header = header.substring(pos.intValue(), pos.intValue() + length);
                }
            }
        }
        float headerVersion = 1.4f;
        try {
            String[] headerParts = header.split("-");
            if (headerParts.length == 2) {
                headerVersion = Float.parseFloat(headerParts[1]);
            }
        } catch (NumberFormatException e) {
            LOGGER.log(Level.WARNING, "Can't parse the header version.", e);
        }

        result.setVersion(headerVersion);
        checkComment(result);

        // rewind
        source.seek(0);
        return result;
    }

    public boolean isLinearized() {
        try {
            COSObject linDict = findFirstDictionary();

            if (linDict != null && !linDict.empty() && linDict.getType() == COSObjType.COS_DICT) {
                if (linDict.knownKey(ASAtom.LINEARIZED).booleanValue()) {
                    long length = linDict.getIntegerKey(ASAtom.L).longValue();
                    if (length != 0) {
                        return length == this.source.getStreamLength() && this.source.getOffset() < LINEARIZATION_DICTIONARY_LOOKUP_SIZE;
                    }
                }
            }
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "IO error while trying to find first document dictionary", e);
        }

        return false;
    }

    private COSObject findFirstDictionary() throws IOException {
        source.seek(0L);
        if (findKeyword(Token.Keyword.KW_OBJ, LINEARIZATION_DICTIONARY_LOOKUP_SIZE)) {
            source.unread(7);

            //this will handle situations when linearization dictionary's
            //object number contains more than one digit
            source.unread();
            while (!CharTable.isSpace(this.source.read())) {
                source.unread(2);
            }

            COSObject linDict = getObject(source.getOffset());
            return linDict;
        }
		return null;
    }

    /**
     * check second line of pdf header
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

    public void getXRefInfo(List<COSXRefInfo> infos) throws IOException {
        calculatePostEOFDataSize();
        this.getXRefInfo(infos, Long.valueOf(0L));
    }

    public COSObject getObject(final long offset) throws IOException {
        clear();

        source.seek(offset);

        final Token token = getToken();

        boolean headerOfObjectComplyPDFA = true;
        boolean headerFormatComplyPDFA = true;
        boolean endOfObjectComplyPDFA = true;

        //Check that if offset doesn't point to obj key there is eol character before obj key
        //pdf/a-1b spec, clause 6.1.8
        skipSpaces(false);
        source.seek(source.getOffset() - 1);
        if (!isNextByteEOL()) {
            headerOfObjectComplyPDFA = false;
        }

        nextToken();
        if (token.type != Token.Type.TT_INTEGER) {
            return new COSObject();
        }
        long number = token.integer;

        if ((source.readByte() != 32) || CharTable.isSpace(source.peek())) {
            //check correct spacing (6.1.8 clause)
            headerFormatComplyPDFA = false;
        }

        nextToken();
        if (token.type != Token.Type.TT_INTEGER) {
            return new COSObject();
        }
        long generation = token.integer;

        if ((source.readByte() != 32) || CharTable.isSpace(source.peek())) {
            //check correct spacing (6.1.8 clause)
            headerFormatComplyPDFA = false;
        }

        nextToken();
        if (token.type != Token.Type.TT_KEYWORD &&
                token.keyword != Token.Keyword.KW_OBJ) {
            return new COSObject();
        }

        this.keyOfCurrentObject = new COSKey((int) number, (int) generation);

        if (!isNextByteEOL()) {
            // eol marker shall follow the "obj" keyword
            headerOfObjectComplyPDFA = false;
        }

        COSObject obj = nextObject();

        skipSpaces();
        this.source.unread();
        if (!isNextByteEOL()) {
            endOfObjectComplyPDFA = false;
        }

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

        if (!isNextByteEOL()) {
            endOfObjectComplyPDFA = false;
        }

        obj.setIsHeaderOfObjectComplyPDFA(Boolean.valueOf(headerOfObjectComplyPDFA));
        obj.setIsHeaderFormatComplyPDFA(Boolean.valueOf(headerFormatComplyPDFA));
        obj.setIsEndOfObjectComplyPDFA(Boolean.valueOf(endOfObjectComplyPDFA));

        return obj;
    }

    private void clear() {
        this.objects.clear();
        this.integers.clear();
        this.flag = true;
    }

    private Long findLastXRef() throws IOException {
        source.seekFromEnd(STARTXREF.length);
        byte[] buf = new byte[STARTXREF.length];
        while (source.getStreamLength() - source.getOffset() < 64) {
            long res = source.getOffset();
            source.read(buf);
            if (Arrays.equals(buf, STARTXREF)) {
                nextToken();
                return this.getToken().integer;
            }
            source.seekFromCurrentPosition(-STARTXREF.length - 1);
        }
        return Long.valueOf(0L);
    }

    private void calculatePostEOFDataSize() throws IOException {
        final byte lookupSize = 64;

        source.seekFromEnd(lookupSize);
        byte[] buffer = new byte[lookupSize];
        source.read(buffer, lookupSize);

        byte postEOFDataSize = -1;

        byte patternSize = (byte) EOF_MARKER.length;
        byte currentMarkerOffset = (byte) (patternSize - 1);
        byte lookupByte = EOF_MARKER[currentMarkerOffset];

        byte currentBufferOffset = lookupSize - 1;

        while (currentBufferOffset >= 0) {
            if (buffer[currentBufferOffset] == lookupByte) {
                if (currentMarkerOffset == 0) {
                    postEOFDataSize = (byte) (lookupSize - currentBufferOffset);
                    postEOFDataSize -= EOF_MARKER.length;
                    if (postEOFDataSize > 0) {
                        if (buffer[currentBufferOffset + EOF_MARKER.length] == 0x0D) {
                            currentBufferOffset++;
                            if (currentBufferOffset + EOF_MARKER.length < buffer.length && buffer[currentBufferOffset  + EOF_MARKER.length] == 0x0A) {
                                postEOFDataSize -= 2;
                                document.setPostEOFDataSize(postEOFDataSize);
                                return;
                            }
							postEOFDataSize -= 1;
							document.setPostEOFDataSize(postEOFDataSize);
							return;
                        } else if (buffer[currentBufferOffset + EOF_MARKER.length] == 0x0A) {
                            postEOFDataSize -= 1;
                            document.setPostEOFDataSize(postEOFDataSize);
                            return;
                        }
                    } else {
                        document.setPostEOFDataSize(postEOFDataSize);
                        return;
                    }
                }
                currentMarkerOffset--;
                // found current char
                lookupByte = EOF_MARKER[currentMarkerOffset];
            } else if (currentMarkerOffset < patternSize - 1) {
                //reset marker
                currentMarkerOffset = patternSize;
                lookupByte = EOF_MARKER[currentMarkerOffset];
            }
            currentBufferOffset--;
        }

        document.setPostEOFDataSize(postEOFDataSize);
    }

    private void getXRefSectionAndTrailer(final COSXRefInfo section) throws IOException {
        if (this.lastTrailerOffset == 0) {
            this.lastTrailerOffset = this.source.getOffset();
        }
        nextToken();
        if ((getToken().type != Token.Type.TT_KEYWORD ||
                getToken().keyword != Token.Keyword.KW_XREF) &&
                (getToken().type != Token.Type.TT_INTEGER)) {
            closeInputStream();
            throw new IOException("PDFParser::GetXRefSection(...)" + StringExceptions.CAN_NOT_LOCATE_XREF_TABLE);
        }
        if (this.getToken().type != Token.Type.TT_INTEGER) { // Parsing usual xref table
            parseXrefTable(section.getXRefSection());
            getTrailer(section.getTrailer());
        } else {
            parseXrefStream(section);
        }
    }

    private void parseXrefTable(final COSXRefSection xrefs) throws IOException {
        //check spacings after "xref" keyword
        //pdf/a-1b specification, clause 6.1.4
        byte space = this.source.readByte();
        if (isCR(space)) {
            if (isLF(this.source.peek())) {
                this.source.readByte();
            }
            if (!isDigit()) {
                document.setXrefEOLMarkersComplyPDFA(false);
            }
        } else if (!isLF(space) || !isDigit()) {
            document.setXrefEOLMarkersComplyPDFA(false);
        }

        nextToken();

        while (getToken().type == Token.Type.TT_INTEGER) {
            //check spacings between header elements
            //pdf/a-1b specification, clause 6.1.4
            space = this.source.readByte();
            if (space != CharTable.ASCII_SPACE || !isDigit()) {
                document.setSubsectionHeaderSpaceSeparated(false);
            }
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
                xref.free = getToken().getValue().charAt(0);
                xrefs.addEntry(number + i, xref);
            }
            nextToken();
        }
        this.source.seekFromCurrentPosition(-7);
    }

    private void parseXrefStream(final COSXRefInfo section) throws IOException {
        nextToken();
        if(this.getToken().type != Token.Type.TT_INTEGER) {
            closeInputStream();
            throw new IOException("PDFParser::GetXRefSection(...)" + StringExceptions.CAN_NOT_LOCATE_XREF_TABLE);
        }
        nextToken();
        if(this.getToken().type != Token.Type.TT_KEYWORD ||
                this.getToken().keyword != Token.Keyword.KW_OBJ) {
            closeInputStream();
            throw new IOException("PDFParser::GetXRefSection(...)" + StringExceptions.CAN_NOT_LOCATE_XREF_TABLE);
        }
        COSObject xrefCOSStream = getDictionary();
        if(!(xrefCOSStream.getType() == COSObjType.COS_STREAM)) {
            throw new IOException("PDFParser::GetXRefSection(...)" + StringExceptions.CAN_NOT_LOCATE_XREF_TABLE);
        }
        XrefStreamParser xrefStreamParser = new XrefStreamParser(section, (COSStream) xrefCOSStream.getDirectBase());
        xrefStreamParser.parseStreamAndTrailer();
        if (section.getTrailer().knownKey(ASAtom.ENCRYPT)) {
            this.isEncrypted = true;
            this.encryption = section.getTrailer().getEncrypt();
            this.id = section.getTrailer().getID();
        }
    }

	private void getXRefInfo(final List<COSXRefInfo> info, Long offset) throws IOException {
		if (offset.longValue() == 0) {
			offset = findLastXRef();
			if (offset.longValue() == 0) {
				closeInputStream();
				throw new IOException("PDFParser::GetXRefInfo(...)" + StringExceptions.START_XREF_VALIDATION);
			}
		}

		clear();

        //for files with junk before header
        if (offsetShift > 0) {
            offset += offsetShift;
        }

        //we will skip eol marker in any case
        source.seek(offset.intValue() - 1);

		COSXRefInfo section = new COSXRefInfo();
		info.add(0, section);

		section.setStartXRef(offset.longValue());
        getXRefSectionAndTrailer(section);

        offset = section.getTrailer().getPrev();
		if (offset == null || offset.longValue() == 0) {
			return;
		}

		getXRefInfo(info, offset);
	}

	private void getTrailer(final COSTrailer trailer) throws IOException {
		if (findKeyword(Token.Keyword.KW_TRAILER)) {
			COSObject obj = nextObject();
			trailer.setObject(obj);
		}

		if (trailer.knownKey(ASAtom.ENCRYPT)) {
		    this.isEncrypted = true;
            this.encryption = trailer.getEncrypt();
            this.id = trailer.getID();
		}
	}

    public boolean isEncrypted() {
        return isEncrypted;
    }

    public COSObject getEncryption() {
        return encryption;
    }

    public COSObject getId() {
        return id;
    }

    public Long getLastTrailerOffset() {
        return lastTrailerOffset;
    }
}
