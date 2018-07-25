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
package org.verapdf.pd.font;

import org.verapdf.as.ASAtom;
import org.verapdf.as.io.ASInputStream;
import org.verapdf.cos.*;
import org.verapdf.pd.font.cff.CFFFontProgram;
import org.verapdf.pd.font.cff.CFFType1FontProgram;
import org.verapdf.pd.font.cmap.CMap;
import org.verapdf.pd.font.opentype.OpenTypeFontProgram;
import org.verapdf.pd.font.truetype.CIDFontType2Program;
import org.verapdf.tools.FontProgramIDGenerator;
import org.verapdf.tools.StaticResources;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class represents CIDFont on PD level.
 *
 * @author Sergey Shemyakov
 */
public class PDCIDFont extends PDFont {

    private static final Logger LOGGER = Logger.getLogger(PDCIDFont.class.getCanonicalName());
    private static final Double DEFAULT_CID_FONT_WIDTH = Double.valueOf(1000d);

    protected CMap cMap;
    private CIDWArray widths;
    private PDCIDSystemInfo cidSystemInfo;

    /**
     * Constructor from COSDictionary and CMap with code -> cid mapping.
     *
     * @param dictionary is COSDictionary of CIDFont.
     * @param cMap       is CMap object containing mapping code -> cid.
     */
    public PDCIDFont(COSDictionary dictionary, CMap cMap) {
        super(dictionary);
        this.cMap = cMap;
    }

    /**
     * Constructor that sets font program for this CIDFont. Can be used when
     * font program should not be parsed twice and is already read.
     *
     * @param dictionary   is COSDictionary of CIDFont.
     * @param cMap         is CMap object containing mapping code -> cid.
     * @param fontProgram  is embedded font program associated with this CIDFont.
     * @param isFontParsed is true if embedded font program has been already
     *                     parsed.
     */
    public PDCIDFont(COSDictionary dictionary, CMap cMap, FontProgram fontProgram,
                     boolean isFontParsed) {
        this(dictionary, cMap);
        this.fontProgram = fontProgram;
        this.isFontParsed = isFontParsed;
        if (fontProgram != null) {
            this.isFontParsed = true;
        }
    }

    /*
        Do not forget to set cMap!!!
     */
    protected PDCIDFont(COSDictionary dictionary) {
        super(dictionary);
    }

    /**
     * @return a stream identifying which CIDs are present in the CIDFont file.
     */
    public COSStream getCIDSet() {
        COSObject cidSet = this.fontDescriptor.getKey(ASAtom.CID_SET);
        return cidSet == null ? null : (COSStream) cidSet.getDirectBase();
    }

    /**
     * @return a specification of the mapping from CIDs to glyph indices if
     * CIDFont is a Type 2 CIDFont.
     */
    public COSObject getCIDToGIDMap() {
        return this.dictionary.getKey(ASAtom.CID_TO_GID_MAP);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Double getWidth(int code) {
        if (this.widths == null) {
            COSObject w = this.dictionary.getKey(ASAtom.W);
            if (w.empty() || w.getType() != COSObjType.COS_ARRAY) {
                return getDefaultWidth();
            }
            this.widths = new CIDWArray((COSArray) w.getDirectBase());
        }
        Double res = widths.getWidth(this.cMap.toCID(code));
        if (res == null) {
            res = getDefaultWidth();
        }
        return res;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Double getDefaultWidth() {
        COSObject dw = this.dictionary.getKey(ASAtom.DW);
        if (dw.getType().isNumber()) {
            return dw.getReal();
        } else {
            return DEFAULT_CID_FONT_WIDTH;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int readCode(InputStream stream) throws IOException {
        if (cMap != null) {
            return cMap.getCodeFromStream(stream);
        }
        throw new IOException("No CMap for Type 0 font " +
                (this.getName() == null ? "" : this.getName()));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public FontProgram getFontProgram() {
        if (!this.isFontParsed) {
            this.isFontParsed = true;

            if (fontDescriptor.canParseFontFile(ASAtom.FONT_FILE2) &&
                    this.getSubtype() == ASAtom.CID_FONT_TYPE2) {
                COSStream trueTypeFontFile = fontDescriptor.getFontFile2();
                COSKey key = trueTypeFontFile.getObjectKey();
                COSObject cidToGIDMap = this.getCIDToGIDMap();
                String fontProgramID = FontProgramIDGenerator.getCIDFontType2ProgramID(key, this.cMap, cidToGIDMap);
                this.fontProgram = StaticResources.getCachedFont(fontProgramID);
                if (fontProgram == null) {
                    try (ASInputStream fontData = trueTypeFontFile.getData(COSStream.FilterFlags.DECODE)) {
                        this.fontProgram = new CIDFontType2Program(
                                fontData, this.cMap, cidToGIDMap);
                        StaticResources.cacheFontProgram(fontProgramID, this.fontProgram);
                    } catch (IOException e) {
                        LOGGER.log(Level.FINE, "Can't read TrueType font program.", e);
                    }
                }
            } else if (fontDescriptor.canParseFontFile(ASAtom.FONT_FILE3)) {
                COSStream fontFile = fontDescriptor.getFontFile3();
                COSName subtype = (COSName) fontFile.getKey(ASAtom.SUBTYPE).getDirectBase();
                COSKey key = fontFile.getObjectKey();
                try {
                    boolean isSubset = this.isSubset();
                    if (ASAtom.CID_FONT_TYPE0C == subtype.getName()) {
                        String fontProgramID = FontProgramIDGenerator.getCFFFontProgramID(key, this.cMap, isSubset);
                        this.fontProgram = StaticResources.getCachedFont(fontProgramID);
                        if (fontProgram == null) {
                            try (ASInputStream fontData = fontFile.getData(COSStream.FilterFlags.DECODE)) {
                                this.fontProgram = new CFFFontProgram(fontData, this.cMap, isSubset);
                                StaticResources.cacheFontProgram(fontProgramID, this.fontProgram);
                            }
                        }
                    } else if (ASAtom.OPEN_TYPE == subtype.getName()) {
                        ASAtom fontName = ASAtom.getASAtom(this.getName());
                        boolean isCFF = fontName != ASAtom.TRUE_TYPE && fontName != ASAtom.CID_FONT_TYPE2;
                        boolean isSymbolic = this.isSymbolic();
                        COSObject encoding = this.getEncoding();
                        String fontProgramID = FontProgramIDGenerator.getOpenTypeFontProgramID(key, isCFF, isSymbolic, encoding, this.cMap, isSubset);
                        this.fontProgram = StaticResources.getCachedFont(fontProgramID);
                        if (fontProgram == null) {
                            try (ASInputStream fontData = fontFile.getData(COSStream.FilterFlags.DECODE)) {
                                this.fontProgram = new OpenTypeFontProgram(
                                        fontData, isCFF, isSymbolic, encoding,
                                        this.cMap, isSubset);
                                StaticResources.cacheFontProgram(fontProgramID, this.fontProgram);
                            }
                        }
                    }
                } catch (IOException e) {
                    LOGGER.log(Level.FINE, "Can't read font program.", e);
                }
            } else {
                this.fontProgram = null;
            }
        }
        return this.fontProgram;
    }

    @Override
    public float getWidthFromProgram(int code) {
        int cid = this.cMap.toCID(code);
        FontProgram font = getFontProgram();
        CFFType1FontProgram cffType1 = CFFType1FontProgram.getCFFType1(font);
        if (cid != 0 && cffType1 != null) {
            // In this case we ignore internal notations of names from CFF
            // Type 1 font and use external CMap
            return cffType1.getWidthFromGID(cid);
        }
        return font.getWidth(code);
    }

    @Override
    public boolean glyphIsPresent(int code) {
        int cid = this.cMap.toCID(code);
        FontProgram font = getFontProgram();
        CFFType1FontProgram cffType1 = CFFType1FontProgram.getCFFType1(font);
        if (cid != 0 && cffType1 != null) {
            // In this case we ignore internal notations of names from CFF
            // Type 1 font and use external CMap
            return cffType1.containsGID(cid);
        }
        return font.containsCode(code);
    }

    /**
     * @return CID System Info object for this CIDFont.
     */
    public PDCIDSystemInfo getCIDSystemInfo() {
        if (this.cidSystemInfo != null) {
            return this.cidSystemInfo;
        } else {
            this.cidSystemInfo =
                    new PDCIDSystemInfo(this.dictionary.getKey(ASAtom.CID_SYSTEM_INFO));
            return this.cidSystemInfo;
        }
    }
}
