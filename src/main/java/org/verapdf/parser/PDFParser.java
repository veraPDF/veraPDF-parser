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
import org.verapdf.as.exceptions.StringExceptions;
import org.verapdf.cos.*;
import org.verapdf.cos.xref.COSXRefEntry;
import org.verapdf.cos.xref.COSXRefInfo;
import org.verapdf.cos.xref.COSXRefSection;
import org.verapdf.exceptions.LoopedException;
import org.verapdf.io.SeekableInputStream;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Timur Kamalov
 */
public class PDFParser extends SeekableCOSParser {

    private static final Logger LOGGER = Logger.getLogger(PDFParser.class.getCanonicalName());

    /**
     * Linearization dictionary must be in first 1024 bytes of document
     */
    protected static final int LINEARIZATION_DICTIONARY_LOOKUP_SIZE = 1024;
    private static final String HEADER_PATTERN = "%PDF-";
    private static final String PDF_DEFAULT_VERSION = "1.4";
    private static final byte[] STARTXREF = "startxref".getBytes(StandardCharsets.ISO_8859_1);

    //%%EOF marker byte representation
    private static final byte[] EOF_MARKER = {37, 37, 69, 79, 70};

    private long offsetShift = 0;
    private boolean isEncrypted;
    private COSObject encryption;
    private Long lastTrailerOffset = 0L;
    
    private COSObject lastXRefStream;
    private boolean containsXRefStream;

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
        return this.getSource();
    }

    private COSHeader parseHeader() throws IOException {
        COSHeader result = new COSHeader();

        String header = getBaseParser().getLine(0);
        if (!header.contains(HEADER_PATTERN)) {
            header = getBaseParser().getLine();
            while (!header.contains(HEADER_PATTERN) && !header.contains(HEADER_PATTERN.substring(1))) {
                if ((!header.isEmpty()) && (Character.isDigit(header.charAt(0)))) {
                    break;
                }
                header = getBaseParser().getLine();
            }
        }

        do {
            getSource().unread();
        } while (getBaseParser().isNextByteEOL());
        getSource().readByte();

        final int headerStart = header.indexOf(HEADER_PATTERN);
        final long headerOffset = getSource().getOffset() - header.length() + headerStart;

        this.offsetShift = headerOffset;
        result.setHeaderOffset(headerOffset);
        result.setHeader(header);

        getBaseParser().skipSingleEol();

        if (headerStart > 0) {
            //trim off any leading characters
            header = header.substring(headerStart);
        }

        // This is used if there is garbage after the header on the same line
        if (header.startsWith(HEADER_PATTERN) && !header.matches(HEADER_PATTERN + "\\d.\\d")) {
            if (header.length() < HEADER_PATTERN.length() + 3) {
                // No version number at all, set to 1.4 as default
                header = HEADER_PATTERN + PDF_DEFAULT_VERSION;
                LOGGER.log(Level.WARNING, getErrorMessage("No version found, set to " + PDF_DEFAULT_VERSION + " as default"));
            } else {
                // trying to parse header version if it has some garbage
                Integer pos = null;
                if (header.indexOf(37) > -1) {
                    pos = header.indexOf(37);
                } else if (header.contains("PDF-")) {
                    pos = header.indexOf("PDF-");
                }
                if (pos != null) {
                    int length = Math.min(8, header.substring(pos).length());
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
        } catch (NumberFormatException e) {
            LOGGER.log(Level.FINE, getErrorMessage("Can't parse the document header"), e);
        }

        result.setVersion(headerVersion);
        checkComment(result);

        // rewind
        getSource().seek(0);
        return result;
    }

    public boolean isLinearized() {
        try {
            COSObject linDict = findFirstDictionary();

            if (isLinearizationDictionary(linDict)) {
                long length = linDict.getIntegerKey(ASAtom.L);
                if (length != 0) {
                    return length == this.getSource().getStreamLength() && this.getSource().getOffset() < LINEARIZATION_DICTIONARY_LOOKUP_SIZE;
                }
            }
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "IO error while trying to find first document dictionary", e);
        }

        return false;
    }

    public COSObject getLinearizationDictionary() {
        try {
            COSObject linDict = findFirstDictionary();
            if (isLinearizationDictionary(linDict)) {
                return linDict;
            }
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "IO error while trying to find linearization dictionary", e);
        }
        return null;
    }

    private static boolean isLinearizationDictionary(COSObject object) {
        return object != null && !object.empty() && object.getType() == COSObjType.COS_DICT && object.knownKey(ASAtom.LINEARIZED);
    }

    private COSObject findFirstDictionary() throws IOException {
        getSource().seek(0L);
        if (getBaseParser().findKeyword(Token.Keyword.KW_OBJ, LINEARIZATION_DICTIONARY_LOOKUP_SIZE)) {
            getSource().unread(7);

            //this will handle situations when linearization dictionary's
            //object number contains more than one digit
            getSource().unread();
            while (!CharTable.isSpace(this.getSource().read())) {
                getSource().unread(2);
            }
            return getObject(getSource().getOffset());
        }
		return null;
    }

    /**
     * check second line of pdf header
     */
    private void checkComment(final COSHeader header) throws IOException {
        byte[] comment = getBaseParser().getLineBytes();
        boolean isValidComment = true;

        if (comment != null && comment.length != 0) {
            if (comment[0] != '%') {
                isValidComment = false;
            }

            if (comment.length < 5) {
                isValidComment = false;
            }
        } else {
            isValidComment = false;
        }
        if (isValidComment) {
            header.setBinaryHeaderBytes(comment[1], comment[2],
                    comment[3], comment[4]);
        } else {
            header.setBinaryHeaderBytes(0, 0, 0, 0);
        }
    }

    public void getXRefInfo(List<COSXRefInfo> infos) throws IOException {
        calculatePostEOFDataSize();
        document.setFileSize(getSource().getStreamLength());
        this.getXRefInfo(infos, new HashSet<>(), null);
    }

    public COSObject getObject(final long offset) throws IOException {
        clear();

        getSource().seek(offset);

        final Token token = getBaseParser().getToken();

        boolean headerOfObjectComplyPDFA = true;
        boolean headerFormatComplyPDFA = true;
        boolean endOfObjectComplyPDFA = true;

        //Check that if offset doesn't point to obj key there is eol character before obj key
        //pdf/a-1b spec, clause 6.1.8
        getBaseParser().skipSpaces(false);
        getSource().seek(getSource().getOffset() - 1);
        if (!getBaseParser().isNextByteEOL()) {
            headerOfObjectComplyPDFA = false;
        }
        getSource().skip(1);

        getBaseParser().nextToken();
        if (token.type != Token.Type.TT_INTEGER) {
            return new COSObject();
        }
        long number = token.integer;

        if (!CharTable.isSpace(getSource().read()) || CharTable.isSpace(getSource().peek())) {
            //check correct spacing (6.1.8 clause)
            headerFormatComplyPDFA = false;
        }

        getBaseParser().nextToken();
        if (token.type != Token.Type.TT_INTEGER) {
            return new COSObject();
        }
        long generation = token.integer;

        if (!CharTable.isSpace(getSource().read()) || CharTable.isSpace(getSource().peek())) {
            //check correct spacing (6.1.8 clause)
            headerFormatComplyPDFA = false;
        }

        getBaseParser().nextToken();
        if (token.type != Token.Type.TT_KEYWORD &&
                token.keyword != Token.Keyword.KW_OBJ) {
            return new COSObject();
        }

        this.keyOfCurrentObject = new COSKey((int) number, (int) generation);
        if (this.document.isReaderInitialized() &&
                this.document.getOffset(keyOfCurrentObject) == 0) {
            return new COSObject();
        }

        if (!getBaseParser().isNextByteEOL()) {
            // eol marker shall follow the "obj" keyword
            headerOfObjectComplyPDFA = false;
        }

        COSObject obj = nextObject();

        if (obj.getType() == COSObjType.COS_STREAM) {
            try {
                if (this.document.isEncrypted()) {
                    this.document.getStandardSecurityHandler().decryptStream(
                            (COSStream) obj.getDirectBase(), new COSKey((int) number,
                                    (int) generation));
                }
            } catch (GeneralSecurityException e) {
                throw new IOException(getErrorMessage("Stream cannot be decrypted"), e);
            }
        }

        long beforeSkip = this.getSource().getOffset();
        getBaseParser().skipSpaces();
        if (this.getSource().getOffset() != beforeSkip) {
            this.getSource().unread();
        }
        if (!getBaseParser().isNextByteEOL()) {
            endOfObjectComplyPDFA = false;
        }

        long offsetBeforeEndobj = this.getSource().getOffset();
        if (this.flag) {
            getBaseParser().nextToken();
        }
        this.flag = true;

        if (token.type != Token.Type.TT_KEYWORD &&
                token.keyword != Token.Keyword.KW_ENDOBJ) {
            // TODO : replace with ASException
            LOGGER.log(Level.WARNING, getErrorMessage("No endobj keyword " + offsetBeforeEndobj));
            this.getSource().seek(offsetBeforeEndobj);
        }

        if (!getBaseParser().isNextByteEOL()) {
            endOfObjectComplyPDFA = false;
        }

        obj.setIsHeaderOfObjectComplyPDFA(headerOfObjectComplyPDFA);
        obj.setIsHeaderFormatComplyPDFA(headerFormatComplyPDFA);
        obj.setIsEndOfObjectComplyPDFA(endOfObjectComplyPDFA);

        return obj;
    }

    private void clear() {
        this.objects.clear();
        this.integers.clear();
        this.flag = true;
    }

    private Long findLastXRef() throws IOException {
        getSource().seekFromEnd(STARTXREF.length);
        byte[] buf = new byte[STARTXREF.length];
        while (getSource().getStreamLength() - getSource().getOffset() < 1024) {
            getSource().read(buf);
            if (Arrays.equals(buf, STARTXREF)) {
                getBaseParser().nextToken();
                return getBaseParser().getToken().integer;
            }
            if (getSource().getOffset() <= STARTXREF.length) {
                throw new IOException("Document doesn't contain startxref keyword");
            }
            getSource().seekFromCurrentPosition(-STARTXREF.length - 1);
        }
        return null;
    }

    private void calculatePostEOFDataSize() throws IOException {
        long size = getSource().getStreamLength();
        final int lookupSize = 1024 > size ? (int) size : 1024;

        getSource().seekFromEnd(lookupSize);
        byte[] buffer = new byte[lookupSize];
        getSource().read(buffer, lookupSize);

        byte postEOFDataSize = -1;

        byte patternSize = (byte) EOF_MARKER.length;
        byte currentMarkerOffset = (byte) (patternSize - 1);
        byte lookupByte = EOF_MARKER[currentMarkerOffset];

        int currentBufferOffset = lookupSize - 1;

        while (currentBufferOffset >= 0) {
            if (buffer[currentBufferOffset] == lookupByte) {
                if (currentMarkerOffset == 0) {
                    postEOFDataSize = (byte) (lookupSize - currentBufferOffset);
                    postEOFDataSize -= EOF_MARKER.length;
                    if (postEOFDataSize > 0) {
                        if (buffer[currentBufferOffset + EOF_MARKER.length] == 0x0D) {
                            currentBufferOffset++;
                            if (currentBufferOffset + EOF_MARKER.length < buffer.length && buffer[currentBufferOffset + EOF_MARKER.length] == 0x0A) {
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
                        } else {
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
                currentMarkerOffset = (byte) (patternSize - 1);
                lookupByte = EOF_MARKER[currentMarkerOffset];
            }
            currentBufferOffset--;
        }

        document.setPostEOFDataSize(postEOFDataSize);
    }

    private void getXRefSectionAndTrailer(final COSXRefInfo section) throws IOException {
        boolean isLastTrailer = false;
        if (this.lastTrailerOffset == 0) {
            isLastTrailer = true;
            this.lastTrailerOffset = this.getSource().getOffset();
        }
        getBaseParser().nextToken();
        if ((getBaseParser().getToken().type != Token.Type.TT_KEYWORD ||
                getBaseParser().getToken().keyword != Token.Keyword.KW_XREF) &&
                (getBaseParser().getToken().type != Token.Type.TT_INTEGER)) {
            throw new IOException(StringExceptions.CAN_NOT_LOCATE_XREF_TABLE);
        }
        if (this.getBaseParser().getToken().type != Token.Type.TT_INTEGER) { // Parsing usual xref table
            parseXrefTable(section.getXRefSection());
            getTrailer(section.getTrailer());
        } else {
            parseXrefStream(section, isLastTrailer);
        }
    }

    protected void parseXrefTable(final COSXRefSection xrefs) throws IOException {
        //check spacings after "xref" keyword
        //pdf/a-1b specification, clause 6.1.4
        byte space = this.getSource().readByte();
        if (BaseParser.isCR(space)) {
            if (BaseParser.isLF(this.getSource().peek())) {
                this.getSource().readByte();
            }
            if (!getBaseParser().isDigit()) {
                document.setXrefEOLMarkersComplyPDFA(false);
            }
        } else if (!BaseParser.isLF(space) || !getBaseParser().isDigit()) {
            document.setXrefEOLMarkersComplyPDFA(false);
        }

        getBaseParser().nextToken();

        while (getBaseParser().getToken().type == Token.Type.TT_INTEGER) {
            //check spacings between header elements
            //pdf/a-1b specification, clause 6.1.4
            space = this.getSource().readByte();
            if (space != CharTable.ASCII_SPACE || !getBaseParser().isDigit()) {
                document.setSubsectionHeaderSpaceSeparated(false);
            }
            int number = (int) getBaseParser().getToken().integer;
            getBaseParser().nextToken();
            int count = (int) getBaseParser().getToken().integer;
            COSXRefEntry xref;
            for (int i = 0; i < count; ++i) {
                xref = new COSXRefEntry();
                getBaseParser().nextToken();
                xref.offset = getBaseParser().getToken().integer;
                getBaseParser().nextToken();
                xref.generation = (int) getBaseParser().getToken().integer;
                getBaseParser().nextToken();
                String value = getBaseParser().getToken().getValue();
                if (value.isEmpty()) {
                    throw new IOException(getErrorMessage("Failed to parse xref table"));
                }
                xref.free = value.charAt(0);
                if (i == 0 && COSXRefEntry.FIRST_XREF_ENTRY.equals(xref) && number != 0) {
                    number = 0;
                    LOGGER.log(Level.WARNING, getErrorMessage("Incorrect xref section"));
                }
                xrefs.addEntry(number + i, xref);

                checkXrefTableEntryLastBytes();
            }
            getBaseParser().nextToken();
        }
        this.getSource().seekFromCurrentPosition(-7);
    }

    /**
     * Checks that last bytes in the entry of Xref table should be:
     * EOL(CRLF), or Space and LF, or Space and CR
     *
     * @throws IOException - incorrect reading from file
     */
    private void checkXrefTableEntryLastBytes() throws IOException {
        boolean isLastBytesCorrect;

        byte ch = this.getSource().readByte();
        if (BaseParser.isCR(ch)) {
            ch = this.getSource().readByte();
            isLastBytesCorrect = BaseParser.isLF(ch);
        } else if (ch == CharTable.ASCII_SPACE) {
            ch = this.getSource().readByte();
            isLastBytesCorrect = (BaseParser.isLF(ch) || BaseParser.isCR(ch));
        } else {
            isLastBytesCorrect = false;
        }

        if (!isLastBytesCorrect){
            this.getSource().unread();
            LOGGER.log(Level.WARNING, getErrorMessage("Incorrect end of line in cross-reference table"));
        }
    }

    private void parseXrefStream(final COSXRefInfo section, boolean isLastTrailer) throws IOException {
        getBaseParser().nextToken();
        if (this.getBaseParser().getToken().type != Token.Type.TT_INTEGER) {
            throw new IOException(StringExceptions.CAN_NOT_LOCATE_XREF_TABLE);
        }
        getBaseParser().nextToken();
        if (this.getBaseParser().getToken().type != Token.Type.TT_KEYWORD ||
                this.getBaseParser().getToken().keyword != Token.Keyword.KW_OBJ) {
            throw new IOException(StringExceptions.CAN_NOT_LOCATE_XREF_TABLE);
        }
        COSObject xrefCOSStream;
        try {
            xrefCOSStream = getDictionary();
        } catch (Exception e) {
            throw new IOException(getErrorMessage("Exception during parsing xref stream"), e);
        }
        if (xrefCOSStream.getType() != COSObjType.COS_STREAM ||
                !COSName.construct(ASAtom.XREF).equals(xrefCOSStream.getKey(ASAtom.TYPE))) {
            throw new IOException(StringExceptions.CAN_NOT_LOCATE_XREF_TABLE);
        }
        this.containsXRefStream = true;
        if (isLastTrailer) {
            this.lastXRefStream = xrefCOSStream;
        }
        XrefStreamParser xrefStreamParser = new XrefStreamParser(section, (COSStream) xrefCOSStream.getDirectBase());
        xrefStreamParser.parseStreamAndTrailer();
        if (section.getTrailer().knownKey(ASAtom.ENCRYPT)) {
            this.isEncrypted = true;
            this.encryption = section.getTrailer().getEncrypt();
        }
    }

	private void getXRefInfo(final List<COSXRefInfo> info, Set<Long> processedOffsets, Long offset) throws IOException {
        if (offset == null) {
			offset = findLastXRef();
			if (offset == null) {
				throw new IOException(StringExceptions.START_XREF_VALIDATION);
			}
		}

        if (processedOffsets.contains(offset)) {
            throw new LoopedException(getErrorMessage("XRef loop"));
        }
        processedOffsets.add(offset);

		clear();

        //for files with junk before header
        if (offsetShift > 0) {
            offset += offsetShift;
        }

        //we will skip eol marker in any case
        getSource().seek(Math.max(0, offset - 1));

		COSXRefInfo section = new COSXRefInfo();
		info.add(0, section);

		section.setStartXRef(offset);
        getXRefSectionAndTrailer(section);

        COSTrailer trailer = section.getTrailer();

        offset = trailer.getXRefStm();
        if (offset != null) {
            getXRefInfo(info, processedOffsets, offset);
        }

        offset = trailer.getPrev();
		if (offset != null) {
            getXRefInfo(info, processedOffsets, offset);
		}
	}

	private void getTrailer(final COSTrailer trailer) throws IOException {
		if (getBaseParser().findKeyword(Token.Keyword.KW_TRAILER)) {
			COSObject obj = nextObject();
			if (obj.empty() || obj.getType() != COSObjType.COS_DICT) {
				throw new IOException(getErrorMessage("Trailer is empty or has invalid type"));
			}
			trailer.setObject(obj);
		}

		if (trailer.knownKey(ASAtom.ENCRYPT)) {
		    this.isEncrypted = true;
            this.encryption = trailer.getEncrypt();
		}
	}

    public boolean isEncrypted() {
        return isEncrypted;
    }

    public COSObject getEncryption() {
        return encryption;
    }

    public Long getLastTrailerOffset() {
        return lastTrailerOffset;
    }

    public COSObject getLastXRefStream() {
        return lastXRefStream;
    }

    public boolean isContainsXRefStream() {
        return containsXRefStream;
    }
}
