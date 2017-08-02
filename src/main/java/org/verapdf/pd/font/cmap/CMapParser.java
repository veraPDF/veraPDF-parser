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
package org.verapdf.pd.font.cmap;

import org.verapdf.cos.COSName;
import org.verapdf.cos.COSObject;
import org.verapdf.parser.BaseParser;
import org.verapdf.parser.Token;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class parses CMap files and constructs CMap objects.
 *
 * @author Sergey Shemyakov
 */
public class CMapParser extends BaseParser {

    private static final Logger LOGGER = Logger.getLogger(CMapParser.class.getCanonicalName());
    private COSObject lastCOSName;

    private CMap cMap;

    /**
     * {@inheritDoc}
     */
    public CMapParser(String fileName) throws FileNotFoundException {
        super(fileName);
        cMap = new CMap();
    }

    /**
     * {@inheritDoc}
     */
    public CMapParser(InputStream fileStream) throws IOException {
        super(fileStream);
        cMap = new CMap();
    }

    /**
     * @return constructed CMap.
     */
    public CMap getCMap() {
        return cMap;
    }

    /**
     * Method parses CMap from given source.
     */
    public void parse() throws IOException {
        try {
            initializeToken();
            //Skipping starting comments
            skipSpaces(true);
            while (getToken().type != Token.Type.TT_EOF) {
                nextToken();
                processToken();
            }
        } finally {
            this.source.close();    // We close stream after first reading attempt
        }
    }

    private void processToken() throws IOException {
        switch (getToken().type) {
            case TT_NAME:
                switch (getToken().getValue()) {
                    case "WMode":
                        skipSpaces();
                        readNumber();
                        this.cMap.setwMode((int) getToken().integer);
                        break;
                    case "Registry":
                        nextToken();
                        if (getToken().type.equals(Token.Type.TT_LITSTRING)) {
                            this.cMap.setRegistry(getToken().getValue());
                        } else {
                            throw new IOException("CMap contains invalid /Registry value");
                        }
                        break;
                    case "Ordering":
                        nextToken();
                        if (getToken().type.equals(Token.Type.TT_LITSTRING)) {
                            this.cMap.setOrdering(getToken().getValue());
                        } else {
                            throw new IOException("CMap contains invalid /Ordering value");
                        }
                        break;
                    case "CMapName":
                        nextToken();
                        if (getToken().type.equals(Token.Type.TT_NAME)) {
                            this.cMap.setName(getToken().getValue());
                        } else {
                            throw new IOException("CMap contains invalid /CMapName value");
                        }
                        break;
                    case "Supplement":
                        nextToken();
                        if(getToken().type == Token.Type.TT_INTEGER) {
                            this.cMap.setSupplement((int) getToken().integer);
                        } else {
                            throw new IOException("CMap contains invalid /Supplement value");
                        }
                        break;
                    case "CIDCount":
                        nextToken();
                        checkTokenType(Token.Type.TT_INTEGER, "CIDCount");
                        nextToken();
                        checkTokenType(Token.Type.TT_KEYWORD, "CIDCount");
                        break;
                    default:
                        this.lastCOSName = COSName.construct(getToken().getValue());
                }
                break;
            case TT_INTEGER:
                int listLength = (int) getToken().integer;
                nextToken();
                if (getToken().type != Token.Type.TT_KEYWORD) {
                    break;
                }
                processList(listLength, getToken().getValue());
                break;
            case TT_KEYWORD:
                switch (getToken().getValue()) {
                    case "usecmap":
                        CMap usedCMap = new PDCMap(lastCOSName).getCMapFile();
                        if (usedCMap != null) {
                            this.cMap.useCMap(usedCMap);
                        } else {
                            this.cMap.setUsesNonPredefinedCMap(true);
                            LOGGER.log(Level.FINE, "Can't load predefined CMap with name " + lastCOSName);
                        }
                        break;
                    default:
                }
            //$FALL-THROUGH$
		default:
        }
    }

    private void processList(int listLength, String type) throws IOException {
        if (! type.startsWith("begin")) {
        	return;
        }
        String key = type.substring(5); //skipping leading "begin"
        for (int i = 0; i < listLength; ++i) {
            switch (key) {
                case "codespacerange":
                    readLineCodeSpaceRange();
                    break;
                case "cidrange":
                    readLineCIDRange();
                    break;
                case "notdefrange":
                    readLineNotDefRange();
                    break;
                case "cidchar":
                    readSingleCharMapping();
                    break;
                case "notdefchar":
                    readSingleNotDefMapping();
                    break;
                case "bfchar":
                    readSingleToUnicodeMapping();
                    break;
                case "bfrange":
                    readLineBFRange();
                    break;
                default:
            }
        }
        nextToken();
        if (!getToken().getValue().equals("end" + key)) {
            LOGGER.log(Level.FINE, "Unexpected end of " + key + " in CMap");
        }
    }

    private void readLineCodeSpaceRange() throws IOException {
        nextToken();
        checkTokenType(Token.Type.TT_HEXSTRING, "codespacerange list");
        byte[] begin = getToken().getByteValue();

        nextToken();
        checkTokenType(Token.Type.TT_HEXSTRING, "codespacerange list");
        byte[] end = getToken().getByteValue();

        CodeSpace codeSpace = new CodeSpace(begin, end);

        boolean overlaps = false;
        for (CodeSpace cs : cMap.getCodeSpaces()) {
            if (cs.overlaps(codeSpace)) {
                overlaps = true;
                break;
            }
        }
        if (!overlaps) {
            cMap.getCodeSpaces().add(codeSpace);
            if (begin.length < cMap.shortestCodeSpaceLength) {
                cMap.shortestCodeSpaceLength = begin.length;
            }
        } else {
            LOGGER.log(Level.FINE, "CMap " + cMap.getName() + " has overlapping codespace ranges.");
        }
    }

    private void readLineCIDRange() throws IOException {
        nextToken();
        checkTokenType(Token.Type.TT_HEXSTRING, "cidrange list");
        long cidRangeStart = numberFromBytes(getToken().getByteValue());

        nextToken();
        checkTokenType(Token.Type.TT_HEXSTRING, "cidrange list");
        long cidRangeEnd = numberFromBytes(getToken().getByteValue());

        nextToken();
        checkTokenType(Token.Type.TT_INTEGER, "cidrange list");
        this.cMap.addCidInterval(new CIDInterval((int) cidRangeStart,
                (int) cidRangeEnd, (int) getToken().integer));
    }

    private void readLineNotDefRange() throws IOException {
        nextToken();
        checkTokenType(Token.Type.TT_HEXSTRING, "notdef list");
        long notDefRangeStart = numberFromBytes(getToken().getByteValue());

        nextToken();
        checkTokenType(Token.Type.TT_HEXSTRING, "notdef list");
        long notDefRangeEnd = numberFromBytes(getToken().getByteValue());

        nextToken();
        checkTokenType(Token.Type.TT_INTEGER, "notdef list");
        this.cMap.addNotDefInterval(new NotDefInterval((int) notDefRangeStart,
                (int) notDefRangeEnd, (int) getToken().integer));
    }

    private void readSingleCharMapping() throws IOException {
        nextToken();
        checkTokenType(Token.Type.TT_HEXSTRING, "cidchar");
        long charCode = numberFromBytes(getToken().getByteValue());

        nextToken();
        checkTokenType(Token.Type.TT_INTEGER, "cidchar");
        this.cMap.addSingleCidMapping(new SingleCIDMapping((int) charCode,
                (int) getToken().integer));
    }

    private void readSingleNotDefMapping() throws IOException {
        nextToken();
        checkTokenType(Token.Type.TT_HEXSTRING, "notdefchar");
        long notDefCharCode = numberFromBytes(getToken().getByteValue());

        nextToken();
        checkTokenType(Token.Type.TT_INTEGER, "notdefchar");
        this.cMap.addSingleNotDefMapping(new SingleCIDMapping((int) notDefCharCode,
                (int) getToken().integer));
    }

    private void readSingleToUnicodeMapping() throws IOException {
        nextToken();
        checkTokenType(Token.Type.TT_HEXSTRING, "bfchar");
        long bfCharCode = numberFromBytes(getToken().getByteValue());

        String unicodeName = this.readStringFromUnicodeSequenceToken();
        this.cMap.addUnicodeMapping((int) bfCharCode, unicodeName);
    }

    private void readLineBFRange() throws IOException {
        nextToken();
        checkTokenType(Token.Type.TT_HEXSTRING, "bfrange");
        byte[] rangeBegin = getToken().getByteValue();
        long bfRangeBegin = numberFromBytes(rangeBegin);

        nextToken();
        checkTokenType(Token.Type.TT_HEXSTRING, "bfrange");
        long bfRangeEnd = getBfrangeEndFromBytes(getToken().getByteValue(), rangeBegin);

        nextToken();    // skip [
        if(getToken().type == Token.Type.TT_OPENARRAY) {

            for (long i = bfRangeBegin; i <= bfRangeEnd; ++i) {
                this.cMap.addUnicodeMapping((int) i, readStringFromUnicodeSequenceToken());
            }

            nextToken();    // skip ]
        } else {
            byte[] token = getToken().getByteValue();
            int lastByte = token[token.length - 1] & 0xFF;
            if (lastByte > 255 - bfRangeEnd + bfRangeBegin) {
                bfRangeEnd = 255 + bfRangeBegin - lastByte;
            }
            this.cMap.addUnicodeInterval(new ToUnicodeInterval(bfRangeBegin, bfRangeEnd,
                    numberFromBytes(getToken().getByteValue())));
        }
    }

    static long numberFromBytes(byte[] num) {
        long res = 0;
        for (int i = 0; i < num.length; ++i) {
            res += (num[i] & 0x00FF) << ((num.length - i - 1) * 8);
        }
        return res;
    }

    private static long getBfrangeEndFromBytes(byte[] endRange, byte[] beginRange) {
        long res = 0;
        for (int i = 0; i < endRange.length; ++i) {
            if (i < endRange.length - 1) {
                // getting first hex digits of begin range string.
                // see PDF 32000 2008, 9.10.3: these digits should be the same as
                // in end range strings.
                byte endRangeByte = endRange[i];
                byte beginRangeByte = beginRange[i];
                if (endRangeByte != beginRangeByte) {
                    LOGGER.log(Level.FINE, "Incorrect bfrange in toUnicode CMap: " +
                            "bfrange contains more than 256 code.");
                }
                res += (beginRangeByte & 0x00FF) << ((endRange.length - i - 1) * 8);
            } else {    // getting last two hex digits of end range string
                res += (endRange[i] & 0x00FF) << ((endRange.length - i - 1) * 8);
            }
        }
        return res;
    }

    private String readStringFromUnicodeSequenceToken() throws IOException {
        nextToken();
        if (getToken().type == Token.Type.TT_NAME) {
            return this.getToken().getValue();
        } else if (getToken().type == Token.Type.TT_HEXSTRING) {
            byte[] token = getToken().getByteValue();;
            if (token.length == 1) {
                return new String(token, "ISO-8859-1");
            }
			return new String(token, "UTF-16BE");
        }
        throw new IOException("CMap contains invalid entry in bfchar. Expected "
                + Token.Type.TT_NAME + " or " + Token.Type.TT_HEXSTRING + " but got " + getToken().type);
    }

    private void checkTokenType(Token.Type type, String where) throws IOException {
        if (getToken().type != type) {
            throw new IOException("CMap contains invalid entry in " + where +
                    ". Expected " + type + " but got " + getToken().type);
        }
    }

    @Override
    protected boolean isEndOfComment(byte ch) {
        return isCR(ch) || isFF(ch);
    }
}
