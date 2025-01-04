/**
 * This file is part of veraPDF Parser, a module of the veraPDF project.
 * Copyright (c) 2015-2025, veraPDF Consortium <info@verapdf.org>
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
package org.verapdf.pd.font.opentype;

import org.verapdf.as.io.ASInputStream;
import org.verapdf.as.io.ASMemoryInStream;
import org.verapdf.cos.COSKey;
import org.verapdf.cos.COSObject;
import org.verapdf.io.SeekableInputStream;
import org.verapdf.pd.font.FontProgram;
import org.verapdf.pd.font.cff.CFFFontProgram;
import org.verapdf.pd.font.cmap.CMap;
import org.verapdf.pd.font.truetype.CIDFontType2Program;
import org.verapdf.pd.font.truetype.TrueTypeFontProgram;
import org.verapdf.tools.StaticResources;
import org.verapdf.tools.resource.ASFileStreamCloser;

import java.io.IOException;
import java.util.List;

/**
 * Represents OpenType font program.
 *
 * @author Sergey Shemyakov
 */
public class OpenTypeFontProgram implements FontProgram {

    private static final long CFF = 1128678944;     // "CFF " read as 4-byte unsigned number
    // See TrueTypeFontParser table logic

    private final boolean isCFF;
    private final boolean isCIDFontType2;
    private final boolean isSymbolic;
    private final boolean isSubset;
    private final COSObject encoding;
    private final ASInputStream source;
    private FontProgram font;
    private int numTables;
    private boolean attemptedParsing = false;
    private boolean successfullyParsed = false;
    private final CMap externalCMap;
    private final COSObject cidToGIDMap;
    private final COSKey key;

    /**
     * Constructor from stream, containing font data, and encoding details.
     *
     * @param source     is stream containing font data.
     * @param isSymbolic is true if font is marked as symbolic.
     * @param encoding   is value of /Encoding in font dictionary.
     */
    public OpenTypeFontProgram(ASInputStream source, boolean isCFF, boolean isCIDFontType2, boolean isSymbolic,
                               COSObject encoding, CMap externalCMap, boolean isSubset, COSObject cidToGIDMap, COSKey key) {
        this.source = source;
        this.isCFF = isCFF;
        this.isCIDFontType2 = isCIDFontType2;
        this.isSymbolic = isSymbolic;
        this.encoding = encoding;
        this.externalCMap = externalCMap;
        this.isSubset = isSubset;
        this.cidToGIDMap = cidToGIDMap;
        this.key = key;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public float getWidth(int code) {
        return this.font.getWidth(code);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public float getWidth(String glyphName) {
        return this.font.getWidth(glyphName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean containsCode(int code) {
        return this.font.containsCode(code);
    }

    @Override
    public boolean containsGlyph(String glyphName) {
        return this.font.containsGlyph(glyphName);
    }

    @Override
    public boolean containsCID(int cid) {
        return this.font.containsCID(cid);
    }

    @Override
    public String getGlyphName(int code) {
        return this.font.getGlyphName(code);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isAttemptedParsing() {
        return this.attemptedParsing;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isSuccessfulParsing() {
        return this.successfullyParsed;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void parseFont() throws IOException {
        if (!attemptedParsing) {
            attemptedParsing = true;
            if (isCIDFontType2) {
                this.font = new CIDFontType2Program(source, externalCMap, cidToGIDMap, key);
                this.font.parseFont();
            } else if (isCFF) {
                try (ASInputStream cffTable = getCFFTable()) {
                    this.font = new CFFFontProgram(cffTable, externalCMap, isSubset);
                    this.font.parseFont();
                }
            } else {
                this.font = new TrueTypeFontProgram(source, isSymbolic, encoding, key);
                this.font.parseFont();
            }
            StaticResources.cacheFontProgram(null, this.font);
            this.successfullyParsed = true;
        }
    }

    /**
     * @return CFF font or TrueType font, represented by this OpenType font.
     */
    public FontProgram getFont() {
        return font;
    }

    private ASInputStream getCFFTable() throws IOException {
        try (SeekableInputStream is = SeekableInputStream.getSeekableStream(this.source)) {
            this.readHeader(is);
            for (int i = 0; i < numTables; ++i) {
                long tabName = this.readULong(is);
                this.readULong(is);   // checksum
                long offset = this.readULong(is);
                long length = this.readULong(is);   // length
                if (tabName == CFF) {
                    return is.getStream(offset, length);
                }
            }
        }
        throw new IOException("Can't locate \"CFF \" table in CFF OpenType font program.");
    }

    private void readHeader(ASInputStream is) throws IOException {
        is.skip(4);   // version
        this.numTables = this.readUShort(is);
        is.skip(6);
    }

    private int readUShort(ASInputStream is) throws IOException {
        int highOrder = (is.read() & 0xFF) << 8;
        return highOrder | (is.read() & 0xFF);
    }

    private long readULong(ASInputStream is) throws IOException {
        long res = readUShort(is);
        res = res << 16;
        return res | readUShort(is);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ASFileStreamCloser getFontProgramResource() {
        if (this.source instanceof ASMemoryInStream) {
            return null;
        } else {
            return new ASFileStreamCloser(this.source);
        }
    }

    @Override
    public String getWeight() {
        return null;
    }

    @Override
    public Double getAscent() {
        return null;
    }

    @Override
    public Double getDescent() {
        return null;
    }

    @Override
    public List<Integer> getCIDList() {
        return font.getCIDList();
    }
}
