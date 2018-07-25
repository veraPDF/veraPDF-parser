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
package org.verapdf.pd.font.cff;

import org.verapdf.io.SeekableInputStream;
import org.verapdf.pd.font.FontProgram;
import org.verapdf.pd.font.cmap.CMap;

import java.io.IOException;
import java.util.*;

/**
 * Instance of this class represent a parser of CIDFont from FontSet of CFF file.
 *
 * @author Sergey Shemyakov
 */
public class CFFCIDFontProgram extends CFFFontBaseParser implements FontProgram {

    private long fdArrayOffset;
    private long fdSelectOffset;
    private Map<Integer, Integer> charSet;  // mapping cid -> gid
    private boolean isDefaultCharSet = false;
    private int[] fdSelect;     // array with mapping gid -> font dict
    private int[] nominalWidths;
    private int[] defaultWidths;
    private int supplement;
    private String registry;
    private String ordering;
    private int[] bias;
    private CFFIndex[] localSubrIndexes;
    private float[][] fontMatrices;

    private CMap externalCMap;

    CFFCIDFontProgram(SeekableInputStream stream, CFFIndex definedNames, CFFIndex globalSubrs,
                      long topDictBeginOffset, long topDictEndOffset, CMap externalCMap,
                      boolean isSubset) {
        super(stream);
        this.definedNames = definedNames;
        this.globalSubrs = globalSubrs;
        this.topDictBeginOffset = topDictBeginOffset;
        this.topDictEndOffset = topDictEndOffset;
        this.externalCMap = externalCMap;
        this.isSubset = isSubset;
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

            this.source.seek(charStringsOffset);
            this.readCharStrings();

            this.source.seek(charSetOffset);
            this.readCharSet();

            this.source.seek(fdSelectOffset);
            this.readFDSelect();

            this.source.seek(fdArrayOffset);
            this.readFontDicts();

            this.readWidths();
            this.successfullyParsed = true;
        }
    }

    @Override
    protected void readTopDictTwoByteOps(int lastRead) throws IOException {
        switch (lastRead) {
            case 30:
                this.supplement = (int) this.stack.get(this.stack.size() - 1).getInteger();
                this.ordering = getStringBySID((int)
                        this.stack.get(this.stack.size() - 2).getInteger());
                this.registry = getStringBySID((int)
                        this.stack.get(this.stack.size() - 3).getInteger());
                this.stack.clear();
                break;
            case 36:
                this.fdArrayOffset = this.stack.get(this.stack.size() - 1).getInteger();
                this.stack.clear();
                break;
            case 37:
                this.fdSelectOffset = this.stack.get(this.stack.size() - 1).getInteger();
                this.stack.clear();
                break;
            default:
                this.stack.clear();
        }
    }

    private void readCharSet() throws IOException {
        this.charSet = new HashMap<>(nGlyphs);
        this.charSet.put(0, 0);
        int format = this.readCard8();
        switch (format) {
            case 0:
                for (int i = 1; i < nGlyphs; ++i) {
                    this.charSet.put(this.readCard16(), i);
                }
                break;
            case 1:
            case 2:
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
                        this.charSet.put(first + i, charSetPointer++);
                    }
                }
                break;
            default:
                isDefaultCharSet = true;
        }
    }

    private void readFDSelect() throws IOException {
        try {
            int format = this.readCard8();
            this.fdSelect = new int[nGlyphs];
            if (format == 0) {
                for (int i = 0; i < nGlyphs; ++i) {
                    this.fdSelect[i] = this.readCard8();
                }
            } else if (format == 3) {
                int numberOfRanges = this.readCard16();
                int first = this.readCard16();
                for (int i = 0; i < numberOfRanges; ++i) {
                    int fd = this.readCard8();
                    int afterLast = this.readCard16();
                    for (int j = first; j < afterLast && j < fdSelect.length; ++j) {
                        this.fdSelect[j] = fd;
                    }
                    first = afterLast;
                }
            } else {
                throw new IOException("Can't parse format of FDSelect in CFF file");
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new IOException("Can't parse FDSelect in CFF file", e);
        }
    }

    private void readFontDicts() throws IOException {
        CFFIndex fontDictIndex = this.readIndex();
        this.nominalWidths = new int[fontDictIndex.size()];
        this.defaultWidths = new int[fontDictIndex.size()];
        this.fontMatrices = new float[fontDictIndex.size()][6];
        this.bias = new int[fontDictIndex.size()];
        this.localSubrIndexes = new CFFIndex[fontDictIndex.size()];
        for (int i = 0; i < fontDictIndex.size(); ++i) {
            this.readTopDict(fontDictIndex.getOffset(i) + fdArrayOffset +
                    fontDictIndex.getOffsetShift() - 1,
                    fontDictIndex.getOffset(i + 1) + fdArrayOffset +
                            fontDictIndex.getOffsetShift() - 1);
            this.readPrivateDict(this.privateDictOffset, this.privateDictSize, i);
            this.nominalWidths[i] = this.nominalWidthX;
            this.defaultWidths[i] = this.defaultWidthX;
            this.fontMatrices[i] = this.fontMatrix;
        }
    }

    private void readPrivateDict(long from, long size, int fontDictNum) throws IOException {
        this.stack.clear();
        long startingOffset = this.source.getOffset();
        this.source.seek(from);
        while (this.source.getOffset() < from + size) {
            this.readPrivateDictUnit();
        }
        this.source.seek(startingOffset);
        this.readLocalSubrsAndBias(fontDictNum);
    }

    private void readTopDict(long from, long to) throws IOException {
        this.stack.clear();
        long startingOffset = this.source.getOffset();
        this.source.seek(from);
        while (this.source.getOffset() < to) {
            this.readTopDictUnit();
        }
        this.source.seek(startingOffset);

    }

    private void readWidths() {
        CFFCharStringsHandler charStrings = new CFFCharStringsHandler(
                this.charStrings, this.charStringsOffset, this.source);
        this.widths = new CharStringsWidths(this.isSubset, this.charStringType,
                charStrings, this.fontMatrices, this.localSubrIndexes, this.globalSubrs,
                this.bias, this.defaultWidths, this.nominalWidths, this.fdSelect);
    }

    /**
     * Gets glyph ID for given character ID.
     *
     * @param cid is character ID.
     * @return glyph ID or null if character is not in font.
     */
    private Integer getGid(int cid) {
        if (isDefaultCharSet) {
            return cid;
        }
        return this.charSet.get(cid);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public float getWidth(int code) {
        int cid = this.externalCMap.toCID(code);
        Integer gid = getGid(cid);
        return (gid == null || gid == 0) ? -1 : widths.getWidth(gid);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public float getWidth(String glyphName) {
        // not applicable in this case
        return 0;
    }

    @Override
    public String getGlyphName(int code) {
        return null;  // No need in this method
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean containsCode(int code) {
        int cid = externalCMap.toCID(code);
        return this.externalCMap.containsCode(code) &&
                containsCID(cid);
    }

    @Override
    public boolean containsGlyph(String glyphName) {
        // not applicable in this case
        return false;
    }

    @Override
    public boolean containsCID(int cid) {
        return this.charSet.get(cid) != null &&
                this.charSet.get(cid) != 0;
    }

    public int getSupplement() {
        return supplement;
    }

    public String getRegistry() {
        return registry;
    }

    public String getOrdering() {
        return ordering;
    }

    @Override
    public boolean isAttemptedParsing() {
        return this.attemptedParsing;
    }

    @Override
    public boolean isSuccessfulParsing() {
        return this.successfullyParsed;
    }

    private void readLocalSubrsAndBias(int fontDictNum) throws IOException {
        if (this.subrsOffset != -1) {
            long startOffset = this.source.getOffset();
            this.source.seek(this.subrsOffset);
            this.localSubrIndexes[fontDictNum] = this.readIndex();
            this.source.seek(startOffset);
            int nSubrs = localSubrIndexes[fontDictNum].size();
            if (this.charStringType == 1) {
                this.bias[fontDictNum] = 0;
            } else if (nSubrs < 1240) {
                bias[fontDictNum] = 107;
            } else if (nSubrs < 33900) {
                bias[fontDictNum] = 1131;
            } else {
                bias[fontDictNum] = 32768;
            }
        }
    }

    public List<Integer> getCIDList() {
        if (charSet != null) {
            List<Integer> res = new ArrayList<>(this.charSet.size());
            for (Map.Entry<Integer, Integer> entry : this.charSet.entrySet()) {
                res.add(entry.getKey());
            }
            return res;
        }
        return Collections.emptyList();
    }
}
