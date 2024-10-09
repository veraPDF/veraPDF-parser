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

import org.verapdf.as.ASAtom;
import org.verapdf.as.CharTable;
import org.verapdf.cos.COSDocument;
import org.verapdf.cos.COSKey;
import org.verapdf.cos.COSObjType;
import org.verapdf.cos.COSObject;
import org.verapdf.io.SeekableInputStream;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class is extension of BaseParser for parsing of digital signature dictionaries.
 * It calculates byte range of digital signature.
 *
 * @author Sergey Shemyakov
 */
public class SignatureParser extends SeekableCOSParser {


    private static final Logger LOGGER = Logger.getLogger(SignatureParser.class.getCanonicalName());
    private static final byte[] EOF_STRING = "%%EOF".getBytes(StandardCharsets.ISO_8859_1);
    private static final byte[] STREAM_STRING = "stream".getBytes(StandardCharsets.ISO_8859_1);
    private static final byte[] ENDSTREAM_STRING = "endstream".getBytes(StandardCharsets.ISO_8859_1);

    private boolean isStream = false;
    private final long[] byteRange = new long[4];
    private int floatingBytesNumber = 0;
    private boolean isStreamEnd = true;
    private final COSDocument document;

    /**
     * Constructor.
     *
     * @param stream The stream to read the data from.
     * @throws IOException If there is an error reading the input stream.
     */
    public SignatureParser(SeekableInputStream stream, COSDocument document) throws IOException {
        super(stream);
        this.document = document;
    }


    /**
     * Parses signature dictionary to find Contents key.
     *
     * @throws IOException if problem in reading stream occurs.
     */
    private void parseDictionary()
            throws IOException {
        getBaseParser().skipSpaces();
        getBaseParser().skipExpectedCharacter('<');
        getBaseParser().skipExpectedCharacter('<');
        getBaseParser().skipSpaces();
        boolean done = false;
        while (!done) {
            getBaseParser().skipSpaces();
            char c = (char) getSource().peek();
            if (c == '>') {
                done = true;
            } else {
                if (parseSignatureNameValuePair()) {
                    done = true;
                }
            }
        }
    }

    /**
     * This will pass a PDF dictionary value.
     *
     * @throws IOException If there is an error parsing the dictionary object.
     */
    private void passCOSDictionaryValue() throws IOException {
        long numOffset = getSource().getOffset();
        COSObject number = nextObject();
        getBaseParser().skipSpaces();
        if (!getBaseParser().isDigit()) {
            return;
        }
        COSObject generationNumber = nextObject();
        getBaseParser().skipSpaces();
        getBaseParser().skipExpectedCharacter('R');
        if (number.getType() != COSObjType.COS_INTEGER) {
            throw new IOException(getErrorMessage("expected number but got " + number.getType(), numOffset));
        }
        if (generationNumber.getType() != COSObjType.COS_INTEGER) {
            throw new IOException(getErrorMessage("expected number but got " + generationNumber.getType(), numOffset));
        }
    }

    /**
     * Calculates actual byte range of signature.
     *
     * @return array of 4 longs, which is byte range array.
     */
    public long[] getByteRangeBySignatureOffset(long signatureOffset) throws IOException {
        getSource().seek(signatureOffset);
        skipID();
        byteRange[0] = 0;
        parseDictionary();
        byteRange[3] = getOffsetOfNextEOF(getOffsetOfNextXRef(byteRange[2])) - byteRange[2];
        return byteRange;
    }

    public int getFloatingBytesNumberForLastByteRangeObtained() {
        return this.floatingBytesNumber;
    }

    public boolean isStreamEnd() {
        return isStreamEnd;
    }

    private boolean parseSignatureNameValuePair() throws IOException {
        COSObject key = getName();
        if (key.getType() != COSObjType.COS_NAME) {
            LOGGER.log(Level.FINE, getErrorMessage("Invalid signature dictionary"));
            return false;
        }
        if (key.getName() != ASAtom.CONTENTS) {
            passCOSDictionaryValue();
            return false;
        }
        parseSignatureValue();
        return true;
    }

    private void parseSignatureValue() throws IOException {
        getBaseParser().skipSpaces();
        long numOffset1 = getSource().getOffset();
        COSObject number = nextObject();
        long numOffset2 = getSource().getOffset();
        getBaseParser().skipSpaces();
        if (!getBaseParser().isDigit()) {
            byteRange[1] = numOffset1;
            byteRange[2] = numOffset2;
            return;
        }
        long genOffset = getSource().getOffset();
        COSObject generationNumber = nextObject();
        getBaseParser().skipSpaces();
        int c = getSource().read();
        if (c == 'R') {  // Indirect reference
            if (number.getType() != COSObjType.COS_INTEGER) {
                throw new IOException(getErrorMessage("expected number but got " + number.getType(), numOffset1));
            }
            if (generationNumber.getType() != COSObjType.COS_INTEGER) {
                throw new IOException(getErrorMessage("expected number but got " + generationNumber.getType(), genOffset));
            }
            COSKey key = new COSKey(number.getInteger().intValue(),
                    generationNumber.getInteger().intValue());
            long keyOffset = this.document.getOffset(key);
            getSource().seek(keyOffset + document.getHeader().getHeaderOffset());
            parseSignatureValue();    // Recursive parsing to get to the contents hex string itself
        }
        if (c == 'o') {    // Object itself
            getBaseParser().skipExpectedCharacter('b');
            getBaseParser().skipExpectedCharacter('j');
            getBaseParser().skipSpaces();
            numOffset1 = getSource().getOffset();
            nextObject();
            numOffset2 = getSource().getOffset();
            byteRange[1] = numOffset1;
            byteRange[2] = numOffset2;
        } else {
            throw new IOException(getErrorMessage("\"R\" or \"obj\" expected, but \'" + (char) c + "\' found"));
        }
    }

    private long getOffsetOfNextXRef(long currentOffset) {
        return this.document.getStartXRefs()
                            .stream()
                            .filter(offset -> offset > currentOffset)
                            .findFirst()
                            .orElse(currentOffset);
    }

    /**
     * Scans stream until next %%EOF is found.
     *
     * @param currentOffset byte offset of position, from which scanning stats
     * @return number of byte that contains 'F' in %%EOF
     * @throws IOException
     */
    private long getOffsetOfNextEOF(long currentOffset) throws IOException {
        byte[] buffer = new byte[EOF_STRING.length];
        getSource().seek(currentOffset + document.getHeader().getHeaderOffset());
        getSource().read(buffer);
        getSource().unread(buffer.length - 1);
        isStream = false;
        while (isStream || !isEOFFound(buffer)) {
            byte[] streamCheck = isStream ? ENDSTREAM_STRING : STREAM_STRING;
            byte[] streamCheckBuff = new byte[streamCheck.length];
            int unreadLength = getSource().read(streamCheckBuff);
            if (Arrays.equals(streamCheck, streamCheckBuff)) {
				isStream = !isStream;
				System.arraycopy(streamCheckBuff, 0, buffer, 0, EOF_STRING.length);
				unreadLength = -1;
			} else {
                getSource().unread(unreadLength);
                getSource().read(buffer);
				unreadLength = buffer.length - 1;
			}

            if (getSource().isEOF()) {
                getSource().seek(currentOffset + document.getHeader().getHeaderOffset());
                return getSource().getStreamLength();
            }
            if (unreadLength > 0) {
                getSource().unread(unreadLength);
			}
        }
        long result = getSource().getOffset() - 1 + buffer.length;   // byte right after '%%EOF'
        this.getSource().skip(EOF_STRING.length - 1);
        this.floatingBytesNumber = 0;
        this.isStreamEnd = false;
        int nextByte = this.getSource().read();
        if (BaseParser.isLF(nextByte)) { // allows single LF, CR or CR-LF after %%EOF
            result++;
            this.floatingBytesNumber++;
            nextByte = this.getSource().peek();
        } else if (BaseParser.isCR(nextByte)) {
            result++;
            this.floatingBytesNumber++;
            nextByte = this.getSource().read();
            if (BaseParser.isLF(nextByte)) {
                result++;
                this.floatingBytesNumber++;
                nextByte = this.getSource().peek();
            }
        }
        if (nextByte == -1) {
            this.isStreamEnd = true;
        }
        getSource().seek(currentOffset + document.getHeader().getHeaderOffset());
        return result;
    }

    private boolean isEOFFound(byte[] buffer) throws IOException {
        if (!Arrays.equals(buffer, EOF_STRING)) {
            return false;
        }
        long pointer = this.getSource().getOffset();
        this.getSource().unread(2);
        int byteBeforeEOF = this.getSource().peek();
        while (!BaseParser.isLF(byteBeforeEOF)) {
            this.getSource().unread();
            byteBeforeEOF = this.getSource().peek();
            if (byteBeforeEOF != CharTable.ASCII_SPACE) {
                this.getSource().seek(pointer);
                return false;
            }
        }
        this.getSource().seek(pointer);
        return true;
    }

    private void skipID() throws IOException {
        nextObject();
        this.objects.clear();
        this.flag = true;
    }

}
