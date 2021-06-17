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
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Timur Kamalov
 */
public class PDFParser extends COSParser {

    private static final Logger LOGGER = Logger.getLogger(PDFParser.class.getCanonicalName());

    private static final String HEADER_PATTERN = "%PDF-";
    private static final String PDF_DEFAULT_VERSION = "1.4";
    private static final byte[] STARTXREF = "startxref".getBytes(StandardCharsets.ISO_8859_1);

    //%%EOF marker byte representation
    private static final byte[] EOF_MARKER = new byte[]{37, 37, 69, 79, 70};

    private long offsetShift = 0;
    private boolean isEncrypted;
    private COSObject encryption;
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
            LOGGER.log(Level.FINE, "Can't parse the document header.", e);
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
            return getObject(source.getOffset());
        }
		return null;
    }

    /**
     * check second line of pdf header
     */
    private void checkComment(final COSHeader header) throws IOException {
        byte[] comment = getLineBytes();
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
        this.getXRefInfo(infos, new HashSet<Long>(), Long.valueOf(0L));
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
        source.skip(1);

        nextToken();
        if (token.type != Token.Type.TT_INTEGER) {
            return new COSObject();
        }
        long number = token.integer;

        if (!CharTable.isSpace(source.read()) || CharTable.isSpace(source.peek())) {
            //check correct spacing (6.1.8 clause)
            headerFormatComplyPDFA = false;
        }

        nextToken();
        if (token.type != Token.Type.TT_INTEGER) {
            return new COSObject();
        }
        long generation = token.integer;

        if (!CharTable.isSpace(source.read()) || CharTable.isSpace(source.peek())) {
            //check correct spacing (6.1.8 clause)
            headerFormatComplyPDFA = false;
        }

        nextToken();
        if (token.type != Token.Type.TT_KEYWORD &&
                token.keyword != Token.Keyword.KW_OBJ) {
            return new COSObject();
        }

        this.keyOfCurrentObject = new COSKey((int) number, (int) generation);
        if (this.document.isReaderInitialized() &&
                this.document.getOffset(keyOfCurrentObject) == 0) {
            return new COSObject();
        }

        if (!isNextByteEOL()) {
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
                throw new IOException("Stream " + this.keyOfCurrentObject + " cannot be decrypted", e);
            }
        }

        long beforeSkip = this.source.getOffset();
        skipSpaces();
        if (this.source.getOffset() != beforeSkip) {
            this.source.unread();
        }
        if (!isNextByteEOL()) {
            endOfObjectComplyPDFA = false;
        }

        long offsetBeforeEndobj = this.source.getOffset();
        if (this.flag) {
            nextToken();
        }
        this.flag = true;

        if (token.type != Token.Type.TT_KEYWORD &&
                token.keyword != Token.Keyword.KW_ENDOBJ) {
            // TODO : replace with ASException
            LOGGER.log(Level.WARNING, "No endobj keyword at offset " + offsetBeforeEndobj);
            this.source.seek(offsetBeforeEndobj);
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
        while (source.getStreamLength() - source.getOffset() < 1024) {
            source.read(buf);
            if (Arrays.equals(buf, STARTXREF)) {
                nextToken();
                return this.getToken().integer;
            }
            if (source.getOffset() <= STARTXREF.length) {
                throw new IOException("Document doesn't contain startxref keyword");
            }
            source.seekFromCurrentPosition(-STARTXREF.length - 1);
        }
        return Long.valueOf(0L);
    }

    private void calculatePostEOFDataSize() throws IOException {
        long size = source.getStreamLength();
        final int lookupSize = 1024 > size ? (int) size : 1024;

        source.seekFromEnd(lookupSize);
        byte[] buffer = new byte[lookupSize];
        source.read(buffer, lookupSize);

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
        if (this.lastTrailerOffset == 0) {
            this.lastTrailerOffset = this.source.getOffset();
        }
        nextToken();
        if ((getToken().type != Token.Type.TT_KEYWORD ||
                getToken().keyword != Token.Keyword.KW_XREF) &&
                (getToken().type != Token.Type.TT_INTEGER)) {
            throw new IOException("PDFParser::GetXRefSection(...)" + StringExceptions.CAN_NOT_LOCATE_XREF_TABLE);
        }
        if (this.getToken().type != Token.Type.TT_INTEGER) { // Parsing usual xref table
            parseXrefTable(section.getXRefSection());
            getTrailer(section.getTrailer());
        } else {
            parseXrefStream(section);
        }
    }

    protected void parseXrefTable(final COSXRefSection xrefs) throws IOException {
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
                String value = getToken().getValue();
                if (value.isEmpty()) {
                    throw new IOException("Failed to parse xref table");
                }
                xref.free = value.charAt(0);
                if (i == 0 && COSXRefEntry.FIRST_XREF_ENTRY.equals(xref) && number != 0) {
                    number = 0;
                    LOGGER.log(Level.WARNING, "Incorrect xref section");
                }
                xrefs.addEntry(number + i, xref);

                checkXrefTableEntryLastBytes();
            }
            nextToken();
        }
        this.source.seekFromCurrentPosition(-7);
    }

    /**
     * Checks that last bytes in the entry of Xref table should be:
     * EOL(CRLF), or Space and LF, or Space and CR
     *
     * @throws IOException - incorrect reading from file
     */
    private void checkXrefTableEntryLastBytes() throws IOException {
        boolean isLastBytesCorrect;

        byte ch = this.source.readByte();
        if (isCR(ch)) {
            ch = this.source.readByte();
            isLastBytesCorrect = isLF(ch);
        } else if (ch == CharTable.ASCII_SPACE) {
            ch = this.source.readByte();
            isLastBytesCorrect = (isLF(ch) || isCR(ch));
        } else {
            isLastBytesCorrect = false;
        }

        if (!isLastBytesCorrect){
            this.source.unread();
            LOGGER.log(Level.WARNING, "Incorrect end of line in cross-reference table.");
        }
    }

    private void parseXrefStream(final COSXRefInfo section) throws IOException {
        nextToken();
        if (this.getToken().type != Token.Type.TT_INTEGER) {
            throw new IOException("PDFParser::GetXRefSection(...)" + StringExceptions.CAN_NOT_LOCATE_XREF_TABLE);
        }
        nextToken();
        if (this.getToken().type != Token.Type.TT_KEYWORD ||
                this.getToken().keyword != Token.Keyword.KW_OBJ) {
            throw new IOException("PDFParser::GetXRefSection(...)" + StringExceptions.CAN_NOT_LOCATE_XREF_TABLE);
        }
        COSObject xrefCOSStream = getDictionary();
        if (!(xrefCOSStream.getType() == COSObjType.COS_STREAM)) {
            throw new IOException("PDFParser::GetXRefSection(...)" + StringExceptions.CAN_NOT_LOCATE_XREF_TABLE);
        }
        XrefStreamParser xrefStreamParser = new XrefStreamParser(section, (COSStream) xrefCOSStream.getDirectBase());
        xrefStreamParser.parseStreamAndTrailer();
        if (section.getTrailer().knownKey(ASAtom.ENCRYPT)) {
            this.isEncrypted = true;
            this.encryption = section.getTrailer().getEncrypt();
        }
    }

	private void getXRefInfo(final List<COSXRefInfo> info, Set<Long> processedOffsets, Long offset) throws IOException {
        if (offset.longValue() == 0) {
			offset = findLastXRef();
			if (offset.longValue() == 0) {
				throw new IOException("PDFParser::GetXRefInfo(...)" + StringExceptions.START_XREF_VALIDATION);
			}
		}

        if (processedOffsets.contains(offset)) {
            throw new LoopedException("XRef loop");
        }
        processedOffsets.add(offset);

		clear();

        //for files with junk before header
        if (offsetShift > 0) {
            offset += offsetShift;
        }

        //we will skip eol marker in any case
        source.seek(offset.longValue() - 1);

		COSXRefInfo section = new COSXRefInfo();
		info.add(0, section);

		section.setStartXRef(offset.longValue());
        getXRefSectionAndTrailer(section);

        COSTrailer trailer = section.getTrailer();

        offset = trailer.getXRefStm();
        if (offset != null && offset.longValue() != 0) {
            getXRefInfo(info, processedOffsets, offset);
        }

        offset = trailer.getPrev();
		if (offset != null && offset.longValue() != 0) {
            getXRefInfo(info, processedOffsets, offset);
		}
	}

	private void getTrailer(final COSTrailer trailer) throws IOException {
		if (findKeyword(Token.Keyword.KW_TRAILER)) {
			COSObject obj = nextObject();
			if (obj.empty() || obj.getType() != COSObjType.COS_DICT) {
				throw new IOException("Trailer is empty or has invalid type");
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
}
