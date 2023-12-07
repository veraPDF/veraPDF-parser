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

import org.verapdf.as.ASAtom;
import org.verapdf.as.io.ASInputStream;
import org.verapdf.cos.COSObject;
import org.verapdf.parser.Token;
import org.verapdf.parser.postscript.PSObject;
import org.verapdf.parser.postscript.PSParser;
import org.verapdf.parser.postscript.PostScriptException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class parses CMap files and constructs CMap objects.
 *
 * @author Sergey Shemyakov
 */
public class CMapParser extends PSParser {

    private static final Logger LOGGER = Logger.getLogger(CMapParser.class.getCanonicalName());
    private COSObject lastCOSName;

    private final CMap cMap;

    private static final String WMODE_STRING = "WMode";
    private static final String REGISTRY_SRTRING = "Registry";
    private static final String ORDERING_STRING = "Ordering";
    private static final String CMAP_NAME_STRING = "CMapName";
    private static final String SUPPLEMENT_STRING = "Supplement";
    private static final String CID_COUNT_STRING = "CIDCount";

    /**
     * {@inheritDoc}
     */
    public CMapParser(ASInputStream fileStream) throws IOException {
        super(new CMapBaseParser(fileStream));
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
    public void parse() throws IOException, PostScriptException {
        try {
            getBaseParser().initializeToken();
            //Skipping starting comments
            getBaseParser().skipSpaces(true);
            while (getBaseParser().getToken().type != Token.Type.TT_EOF) {
                COSObject nextObject = nextObject();
                processObject(nextObject);
            }
        } finally {
            this.getSource().close();    // We close stream after first reading attempt
        }
        setValuesFromUserDict(this.cMap);
    }

    private void processObject(COSObject object) throws IOException, PostScriptException {
        switch (object.getType()) {
            case COS_INTEGER:
                int listLength = (int) getBaseParser().getToken().integer;
                COSObject nextObject = nextObject();
                if (getBaseParser().getToken().type != Token.Type.TT_KEYWORD ||
                        !processList(listLength, getBaseParser().getToken().getValue())) {
                    PSObject.getPSObject(object).execute(operandStack, userDict);
                    PSObject.getPSObject(nextObject).execute(operandStack, userDict);
                    break;
                }
                break;
            case COS_NAME:
                if ("usecmap".equals(getBaseParser().getToken().getValue())) {
                    CMap usedCMap = new PDCMap(lastCOSName).getCMapFile();
                    if (usedCMap != null) {
                        this.cMap.useCMap(usedCMap);
                    } else {
                        this.cMap.setUsesNonPredefinedCMap(true);
                        LOGGER.log(Level.FINE, "Can't load predefined CMap with name " + lastCOSName);
                    }
                } else {
                    PSObject.getPSObject(object).execute(operandStack, userDict);
                    this.lastCOSName = object;
                }
                break;
            default:
                PSObject.getPSObject(object).execute(operandStack, userDict);
        }
    }

    private boolean processList(int listLength, String type) throws IOException {
        if (!type.startsWith("begin")) {
            return false;
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
                    return false;
            }
        }
        getBaseParser().nextToken();
        if (!getBaseParser().getToken().getValue().equals("end" + key)) {
            LOGGER.log(Level.FINE, "Unexpected end of " + key + " in CMap");
        }
        return true;
    }

    private void readLineCodeSpaceRange() throws IOException {
        getBaseParser().nextToken();
        checkTokenType(Token.Type.TT_HEXSTRING, "codespacerange list");
        byte[] begin = getBaseParser().getToken().getByteValue();

        getBaseParser().nextToken();
        checkTokenType(Token.Type.TT_HEXSTRING, "codespacerange list");
        byte[] end = getBaseParser().getToken().getByteValue();

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
            LOGGER.log(Level.WARNING, "CMap " + cMap.getName() + " has overlapping codespace ranges.");
        }
    }

    private void readLineCIDRange() throws IOException {
        getBaseParser().nextToken();
        checkTokenType(Token.Type.TT_HEXSTRING, "cidrange list");
        long cidRangeStart = numberFromBytes(getBaseParser().getToken().getByteValue());

        getBaseParser().nextToken();
        checkTokenType(Token.Type.TT_HEXSTRING, "cidrange list");
        long cidRangeEnd = numberFromBytes(getBaseParser().getToken().getByteValue());

        getBaseParser().nextToken();
        checkTokenType(Token.Type.TT_INTEGER, "cidrange list");
        this.cMap.addCidInterval(new CIDInterval((int) cidRangeStart,
                (int) cidRangeEnd, (int) getBaseParser().getToken().integer));
    }

    private void readLineNotDefRange() throws IOException {
        getBaseParser().nextToken();
        checkTokenType(Token.Type.TT_HEXSTRING, "notdef list");
        long notDefRangeStart = numberFromBytes(getBaseParser().getToken().getByteValue());

        getBaseParser().nextToken();
        checkTokenType(Token.Type.TT_HEXSTRING, "notdef list");
        long notDefRangeEnd = numberFromBytes(getBaseParser().getToken().getByteValue());

        getBaseParser().nextToken();
        checkTokenType(Token.Type.TT_INTEGER, "notdef list");
        this.cMap.addNotDefInterval(new NotDefInterval((int) notDefRangeStart,
                (int) notDefRangeEnd, (int) getBaseParser().getToken().integer));
    }

    private void readSingleCharMapping() throws IOException {
        getBaseParser().nextToken();
        checkTokenType(Token.Type.TT_HEXSTRING, "cidchar");
        long charCode = numberFromBytes(getBaseParser().getToken().getByteValue());

        getBaseParser().nextToken();
        checkTokenType(Token.Type.TT_INTEGER, "cidchar");
        this.cMap.addSingleCidMapping(new SingleCIDMapping((int) charCode,
                (int) getBaseParser().getToken().integer));
    }

    private void readSingleNotDefMapping() throws IOException {
        getBaseParser().nextToken();
        checkTokenType(Token.Type.TT_HEXSTRING, "notdefchar");
        long notDefCharCode = numberFromBytes(getBaseParser().getToken().getByteValue());

        getBaseParser().nextToken();
        checkTokenType(Token.Type.TT_INTEGER, "notdefchar");
        this.cMap.addSingleNotDefMapping(new SingleCIDMapping((int) notDefCharCode,
                (int) getBaseParser().getToken().integer));
    }

    private void readSingleToUnicodeMapping() throws IOException {
        getBaseParser().nextToken();
        checkTokenType(Token.Type.TT_HEXSTRING, "bfchar");
        long bfCharCode = numberFromBytes(getBaseParser().getToken().getByteValue());

        String unicodeName = this.readStringFromUnicodeSequenceToken();
        this.cMap.addUnicodeMapping((int) bfCharCode, unicodeName);
    }

    private void readLineBFRange() throws IOException {
        getBaseParser().nextToken();
        checkTokenType(Token.Type.TT_HEXSTRING, "bfrange");
        byte[] rangeBegin = getBaseParser().getToken().getByteValue();
        long bfRangeBegin = numberFromBytes(rangeBegin);

        getBaseParser().nextToken();
        checkTokenType(Token.Type.TT_HEXSTRING, "bfrange");
        long bfRangeEnd = getBfrangeEndFromBytes(getBaseParser().getToken().getByteValue(), rangeBegin);

        getBaseParser().nextToken();    // skip [
        if (getBaseParser().getToken().type == Token.Type.TT_OPENARRAY) {

            for (long i = bfRangeBegin; i <= bfRangeEnd; ++i) {
                this.cMap.addUnicodeMapping((int) i, readStringFromUnicodeSequenceToken());
            }

            getBaseParser().nextToken();    // skip ]
        } else {
            byte[] token = getBaseParser().getToken().getByteValue();
            if (token.length == 0) {
                LOGGER.log(Level.WARNING, "Incorrect bfrange in toUnicode CMap: " +
                        "string is empty.");
                return;
            }
            int lastByte = token[token.length - 1] & 0xFF;
            if (lastByte > 255 - bfRangeEnd + bfRangeBegin) {
                LOGGER.log(Level.WARNING, "Incorrect bfrange in toUnicode CMap: " +
                        "the last byte of the string incremented past 255.");
            }
            this.cMap.addUnicodeInterval(new ToUnicodeInterval(bfRangeBegin, bfRangeEnd,
                    getBaseParser().getToken().getByteValue()));
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
        int length = Math.max(beginRange.length, endRange.length);
        byte[] correctBeginRange = new byte[length];
        byte[] correctEndRange = new byte[length];
        System.arraycopy(beginRange, 0, correctBeginRange, length - beginRange.length, beginRange.length);
        System.arraycopy(endRange, 0, correctEndRange, length - endRange.length, endRange.length);
        int i = 0;
        for (; i < length - 1; ++i) {
            // getting first hex digits of begin range string.
            // see PDF 32000 2008, 9.10.3: these digits should be the same as
            // in end range strings.
            byte endRangeByte = correctEndRange[i];
            byte beginRangeByte = correctBeginRange[i];
            if (endRangeByte != beginRangeByte) {
                LOGGER.log(Level.WARNING, "Incorrect bfrange in toUnicode CMap: " +
                        "bfrange contains more than 256 code.");
            }
            res += (beginRangeByte & 0x00FF) << ((length - i - 1) * 8);
        }
        res += (correctEndRange[i] & 0x00FF) << ((length - i - 1) * 8);
        return res;
    }

    private String readStringFromUnicodeSequenceToken() throws IOException {
        getBaseParser().nextToken();
        if (getBaseParser().getToken().type == Token.Type.TT_NAME) {
            return this.getBaseParser().getToken().getValue();
        } else if (getBaseParser().getToken().type == Token.Type.TT_HEXSTRING) {
            byte[] token = getBaseParser().getToken().getByteValue();
            if (token.length == 1) {
                return new String(token, StandardCharsets.ISO_8859_1);
            }
            if (token.length == 2 && token[0] == (byte) 0xFF && token[1] == (byte) 0xFE) {
                return "\uFFFE";
            }
            return new String(token, StandardCharsets.UTF_16BE);
        }
        throw new IOException("CMap contains invalid entry in bfchar. Expected "
                + Token.Type.TT_NAME + " or " + Token.Type.TT_HEXSTRING + " but got " + getBaseParser().getToken().type);
    }

    private void checkTokenType(Token.Type type, String where) throws IOException {
        if (getBaseParser().getToken().type != type) {
            throw new IOException("CMap contains invalid entry in " + where +
                    ". Expected " + type + " but got " + getBaseParser().getToken().type);
        }
    }

    private void setValuesFromUserDict(CMap cMap) {
        cMap.setName(getStringFromUserDict(CMAP_NAME_STRING));
        cMap.setRegistry(getStringFromUserDict(REGISTRY_SRTRING));
        cMap.setOrdering(getStringFromUserDict(ORDERING_STRING));
        cMap.setwMode((int) getLongFromUserDict(WMODE_STRING));
        cMap.setSupplement((int) getLongFromUserDict(SUPPLEMENT_STRING));
    }

    private String getStringFromUserDict(String key) {
        COSObject string = userDict.get(ASAtom.getASAtom(key));
        return string == null ? null : string.getString();
    }

    private long getLongFromUserDict(String key) {
        COSObject number = userDict.get(ASAtom.getASAtom(key));
        if (number == null) {
            return 0;
        }
        Long res = number.getInteger();
        return res == null ? 0 : res;
    }
}
