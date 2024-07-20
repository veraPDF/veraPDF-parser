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
package org.verapdf.pd.font.cff;

import org.verapdf.io.SeekableInputStream;
import org.verapdf.pd.font.FontProgram;
import org.verapdf.pd.font.cmap.CMap;

import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Instance of this class represent a Type1 font from FontSet of
 * CFF file.
 *
 * @author Sergey Shemyakov
 */
public class CFFType1FontProgram extends CFFFontBaseParser implements FontProgram {

    private static final Logger LOGGER = Logger.getLogger(CFFType1FontProgram.class.getCanonicalName());

    private static final String DUPLICATED_GLYPH_NAME_MESSAGE = "CFF Type1 FontProgram contains duplicated glyph name %s";
    
    private static final String NOTDEF_STRING = ".notdef";

    private final CMap externalCMap;  // in case if font is embedded into Type0 font
    private long encodingOffset;
    private final int[] encoding;     // array with mapping code -> gid
    private boolean isStandardEncoding = false;
    private boolean isExpertEncoding = false;
    private Map<String, Integer> charSet;   // mappings glyphName -> gid
    private Map<Integer, String> inverseCharSet;    // mappings gid -> glyph name
    private String[] encodingStrings;
    private int bias;
    private CFFIndex localSubrIndex;


    CFFType1FontProgram(SeekableInputStream stream, CFFIndex definedNames, CFFIndex globalSubrs,
                        long topDictBeginOffset, long topDictEndOffset,
                        CMap externalCMap, boolean isSubset) {
        super(stream, definedNames, globalSubrs, topDictBeginOffset, topDictEndOffset, isSubset);
        encodingOffset = 0;
        encoding = new int[256];
        this.externalCMap = externalCMap;
        fontMatrix = new float[6];
        System.arraycopy(DEFAULT_FONT_MATRIX, 0, this.fontMatrix, 0,
                DEFAULT_FONT_MATRIX.length);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void parseFont() throws IOException {
        if (!attemptedParsing) {
            attemptedParsing = true;
            this.source.seek(topDictBeginOffset);
            while (this.source.getOffset() < topDictEndOffset) {
                readTopDictUnit();
            }
            this.stack.clear();

            this.source.seek(this.privateDictOffset);
            while (this.source.getOffset() < this.privateDictOffset + this.privateDictSize) {
                this.readPrivateDictUnit();
            }
            this.readLocalSubrsAndBias();

            this.source.seek(charStringsOffset);
            this.readCharStrings();

            this.source.seek(encodingOffset);
            this.readEncoding();

            this.source.seek(charSetOffset);
            this.readCharSet();

            this.readWidths();
            this.successfullyParsed = true;
        }
    }

    @Override
    protected void readTopDictOneByteOps(int lastRead) {
        switch (lastRead) {
            case 16:    // encoding
                this.encodingOffset = stack.get(stack.size() - 1).getInteger();
                this.stack.clear();
                break;
            default:
                this.stack.clear();
        }
    }

    private void readEncoding() throws IOException {
        if (encodingOffset == 0) {
            this.isStandardEncoding = true;
        } else if (encodingOffset == 1) {
            this.isExpertEncoding = true;
        } else {
            int format = this.readCard8() & 0xFF;
            int amount;
            switch (format) {
                case 0:
                case 128:
                    amount = this.readCard8() & 0xFF;
                    for (int i = 0; i < amount; ++i) {
                        this.encoding[this.readCard8()] = i;
                    }
                    if (format == 0) {
                        break;
                    }
                    this.readSupplements();
                    break;
                case 1:
                case 129:
                    amount = this.readCard8() & 0xFF;
                    int encodingPointer = 0;
                    for (int i = 0; i < amount; ++i) {
                        int first = this.readCard8() & 0xFF;
                        int nLeft = this.readCard8() & 0xFF;
                        for (int j = 0; j <= nLeft; ++j) {
                            if (first + j < encoding.length) {
                                encoding[(first + j)] = encodingPointer++;
                            }
                        }
                    }
                    if (format == 1) {
                        break;
                    }
                    this.readSupplements();
                    break;
                default:
                    break;
            }
        }
    }

    private void readSupplements() throws IOException {
        int nSups = this.readCard8() & 0xFF;
        for (int i = 0; i < nSups; ++i) {
            int code = this.readCard8() & 0xFF;
            int glyph = this.readCard16();
            encoding[code] = glyph;
        }
    }

    private void readCharSet() throws IOException {
        this.charSet = new HashMap<>();
        this.inverseCharSet = new HashMap<>();
        this.charSet.put(this.getStringBySID(0), 0);
        this.inverseCharSet.put(0, this.getStringBySID(0));
        if (this.charSetOffset == 0) {
            initializeCharSet(CFFPredefined.ISO_ADOBE_CHARSET);
        } else if (this.charSetOffset == 1) {
            initializeCharSet(CFFPredefined.EXPERT_CHARSET);
        } else if (this.charSetOffset == 2) {
            initializeCharSet(CFFPredefined.EXPERT_SUBSET_CHARSET);
        } else {
            int format = this.readCard8();
            switch (format) {
                case 0:
                    for (int i = 1; i < nGlyphs; ++i) {
                        int sid = this.readCard16();
                        String stringBySID = this.getStringBySID(sid);
                        if (charSet.containsKey(stringBySID)) {
                            LOGGER.log(Level.WARNING, String.format(DUPLICATED_GLYPH_NAME_MESSAGE, stringBySID));
                        }
                        this.charSet.put(stringBySID, i);
                        this.inverseCharSet.put(i, stringBySID);
                    }
                    break;
                case 1:
                case 2:
                    try {
                        int charSetPointer = 1;
                        while (charSetPointer < nGlyphs) {
                            int first = this.readCard16();
                            int nLeft;
                            if (format == 1) {
                                nLeft = this.readCard8() & 0xFF;
                            } else {
                                nLeft = this.readCard16();
                            }
                            for (int i = 0; i <= nLeft; ++i) {
                                String stringBySID = this.getStringBySID(first + i);
                                if (charSet.containsKey(stringBySID)) {
                                    LOGGER.log(Level.WARNING, String.format(DUPLICATED_GLYPH_NAME_MESSAGE, stringBySID));
                                }
                                this.charSet.put(stringBySID, charSetPointer);
                                this.inverseCharSet.put(charSetPointer++, stringBySID);
                            }
                        }
                    } catch (ArrayIndexOutOfBoundsException e) {
                        throw new IOException("Error in parsing ranges of CharString in CFF file", e);
                    }
                    break;
                default:
                    throw new IOException("Can't process format of CharSet in CFF file");
            }
        }
    }

    private void readWidths() {
        CFFCharStringsHandler charStrings = new CFFCharStringsHandler(
                this.charStrings, this.charStringsOffset, this.source);
        this.widths = new CharStringsWidths(this.isSubset, this.charStringType,
                charStrings, this.fontMatrix, this.localSubrIndex, this.globalSubrs,
                this.bias, this.defaultWidthX, this.nominalWidthX);
    }

    @Override
    public String getGlyphName(int code) {
        if (code < 0) {
            return NOTDEF_STRING;
        }
        if (isStandardEncoding) {
            if (code < CFFPredefined.STANDARD_ENCODING.length) {
                return CFFPredefined.STANDARD_STRINGS[CFFPredefined.STANDARD_ENCODING[code]];
            }
        } else if (isExpertEncoding) {
            if (code < CFFPredefined.EXPERT_ENCODING.length) {
                return CFFPredefined.STANDARD_STRINGS[CFFPredefined.EXPERT_ENCODING[code]];
            }
        } else if (code < encoding.length && encoding[code] + 1 < inverseCharSet.size()) {
            return inverseCharSet.get(encoding[code] + 1);
        }
        return NOTDEF_STRING;
    }

    @Override
    public Double getAscent() {
        return null;
    }

    @Override
    public Double getDescent() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public float getWidth(int charCode) {
        if(externalCMap != null) {
            int gid = this.externalCMap.toCID(charCode);
            float res = this.widths.getWidth(gid);
            if(res != -1.) {
                return res;
            } else {
                return this.widths.getWidth(0);
            }
        }
        try {
            return this.getWidth(getGlyphName(charCode));
        } catch (ArrayIndexOutOfBoundsException e) {
            return -1;
        }
    }

    public float getWidthFromGID(int gid) {
        return widths.getWidth(gid);
    }

    public boolean containsGID(int gid) {
        return gid >= 0 && this.charStrings.size() > gid;
    }

    @Override
    public boolean containsGlyph(String glyphName) {
        return this.charSet.containsKey(glyphName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public float getWidth(String charName) {
        Integer index = this.charSet.get(charName);
        if (index == null || index >= this.widths.getWidthsAmount() || index < 0) {
            return this.widths.getWidth(0);
        }
        return this.widths.getWidth(index);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean containsCode(int code) {
        return this.charSet.containsKey(this.getGlyphName(code));
    }

    @Override
    public boolean containsCID(int cid) {
        return false;
    }

    private void initializeCharSet(String[] charSetArray) {
        for (int i = 0; i < this.nGlyphs; ++i) {
            charSet.put(charSetArray[i], i);
            inverseCharSet.put(i, charSetArray[i]);
        }
    }

    public String[] getEncoding() {
        if (this.encodingStrings == null) {
            this.encodingStrings = new String[256];
            for(int i = 0; i < 256; ++i) {
                String glyphName = inverseCharSet.get(encoding[i]);
                this.encodingStrings[i] =
                        glyphName == null ? NOTDEF_STRING : glyphName;
            }
        }
        return this.encodingStrings;
    }

    /**
     * @return list of names for all glyphs in this font.
     */
    public Set<String> getCharSet() {
        return this.charSet.keySet();
    }


    @Override
    public boolean isAttemptedParsing() {
        return this.attemptedParsing;
    }

    @Override
    public boolean isSuccessfulParsing() {
        return this.successfullyParsed;
    }

    private void readLocalSubrsAndBias() throws IOException {
        if (this.subrsOffset != -1) {
            long startOffset = this.source.getOffset();
            this.source.seek(this.subrsOffset);
            this.localSubrIndex = this.readIndex();
            this.source.seek(startOffset);
            int nSubrs = localSubrIndex.size();
            if (this.charStringType == 1) {
                this.bias = 0;
            } else if (nSubrs < 1240) {
                bias = 107;
            } else if (nSubrs < 33900) {
                bias = 1131;
            } else {
                bias = 32768;
            }
        }
    }

    /**
     * Gets CFF Type 1 font program for given font program (CFF font program
     * with inner CFF Type 1 or CFF Type 1).
     * @return CFF Type 1 font program or null if no CFF Type 1 font program
     * can be obtained.
     */
    public static CFFType1FontProgram getCFFType1(FontProgram fontProgram) {
        if (fontProgram instanceof CFFType1FontProgram) {
            return (CFFType1FontProgram) fontProgram;
        } else if (fontProgram instanceof CFFFontProgram) {
            FontProgram innerCFF = ((CFFFontProgram) fontProgram).getFont();
            if (innerCFF instanceof CFFType1FontProgram) {
                return (CFFType1FontProgram) innerCFF;
            }
        }
        return null;
    }

    @Override
    public List<Integer> getCIDList() {
        return Collections.emptyList();
    }
}
