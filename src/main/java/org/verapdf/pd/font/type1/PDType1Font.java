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
package org.verapdf.pd.font.type1;

import org.verapdf.as.ASAtom;
import org.verapdf.as.io.ASInputStream;
import org.verapdf.as.io.ASMemoryInStream;
import org.verapdf.cos.*;
import org.verapdf.parser.COSParser;
import org.verapdf.pd.font.Encoding;
import org.verapdf.pd.font.FontProgram;
import org.verapdf.pd.font.PDFontDescriptor;
import org.verapdf.pd.font.PDSimpleFont;
import org.verapdf.pd.font.cff.CFFFontProgram;
import org.verapdf.pd.font.opentype.OpenTypeFontProgram;
import org.verapdf.pd.font.stdmetrics.StandardFontMetrics;
import org.verapdf.pd.font.stdmetrics.StandardFontMetricsFactory;
import org.verapdf.pd.font.truetype.AdobeGlyphList;
import org.verapdf.pd.font.truetype.TrueTypePredefined;
import org.verapdf.tools.FontProgramIDGenerator;
import org.verapdf.tools.StaticResources;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class represents Type 1 font on PD level.
 *
 * @author Sergey Shemyakov
 */
public class PDType1Font extends PDSimpleFont {

    private static final Logger LOGGER = Logger.getLogger(PDType1Font.class.getCanonicalName());
    private static final ASAtom[] STANDARD_FONT_NAMES = {
            ASAtom.COURIER_BOLD,
            ASAtom.COURIER_BOLD_OBLIQUE,
            ASAtom.COURIER,
            ASAtom.COURIER_OBLIQUE,
            ASAtom.HELVETICA,
            ASAtom.HELVETICA_BOLD,
            ASAtom.HELVETICA_BOLD_OBLIQUE,
            ASAtom.HELVETICA_OBLIQUE,
            ASAtom.SYMBOL,
            ASAtom.TIMES_BOLD,
            ASAtom.TIMES_BOLD_ITALIC,
            ASAtom.TIMES_ITALIC,
            ASAtom.TIMES_ROMAN,
            ASAtom.ZAPF_DINGBATS};

    private Boolean isStandard = null;
    private StandardFontMetrics fontMetrics;

    /**
     * Constructor from type 1 font dictionary.
     * @param dictionary is type 1 font dictionary.
     */
    public PDType1Font(COSDictionary dictionary) {
        super(dictionary);
        if (isNameStandard() && this.fontDescriptor.getObject().size() == 0) {
            fontMetrics = StandardFontMetricsFactory.getFontMetrics(this.getName());
            this.fontDescriptor = PDFontDescriptor.getDescriptorFromMetrics(fontMetrics);
        }
    }

    /**
     * @return set of character names defined in font as specified in CIDSet in
     * font descriptor.
     */
    public Set<String> getDescriptorCharSet() {
        String descriptorCharSetString = this.fontDescriptor.getCharSet();
        if (descriptorCharSetString != null) {
            try {
                ASMemoryInStream stream =
                        new ASMemoryInStream(descriptorCharSetString.getBytes());
                Set<String> descriptorCharSet = new TreeSet<>();
                COSParser parser = new COSParser(stream);
                COSObject glyphName = parser.nextObject();
                while (!glyphName.empty()) {
                    if (glyphName.getType() == COSObjType.COS_NAME) {
                        descriptorCharSet.add(glyphName.getString());
                    }
                    glyphName = parser.nextObject();
                }
                return descriptorCharSet;
            } catch (IOException ex) {
                LOGGER.log(Level.FINE, "Can't parse /CharSet entry in Type 1 font descriptor", ex);
                return Collections.emptySet();
            }
        }
        return Collections.emptySet();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public FontProgram getFontProgram() {
        if (!this.isFontParsed) {
            this.isFontParsed = true;
            if (fontDescriptor.canParseFontFile(ASAtom.FONT_FILE)) {
                parseType1FontProgram(ASAtom.FONT_FILE);
            } else if (fontDescriptor.canParseFontFile(ASAtom.FONT_FILE3)) {
                parseType1FontProgram(ASAtom.FONT_FILE3);
            } else {
                this.fontProgram = null;
            }
        }
        return this.fontProgram;
    }

    private void parseType1FontProgram(ASAtom fontFileType) {
        if (fontDescriptor.canParseFontFile(fontFileType)) {
            COSStream type1FontFile = null;
            if (fontFileType == ASAtom.FONT_FILE) {
                type1FontFile = fontDescriptor.getFontFile();
            } else if (fontFileType == ASAtom.FONT_FILE3) {
                type1FontFile = fontDescriptor.getFontFile3();
            }
            if (type1FontFile != null) {
                COSKey key = type1FontFile.getObjectKey();
                try {
                    if (fontFileType == ASAtom.FONT_FILE) {
                        String fontProgramID = FontProgramIDGenerator.getType1FontProgramID(key);
                        this.fontProgram = StaticResources.getCachedFont(fontProgramID);
                        if (fontProgram == null) {
                            try (ASInputStream fontData = type1FontFile.getData(COSStream.FilterFlags.DECODE)) {
                                this.fontProgram = new Type1FontProgram(fontData);
                                StaticResources.cacheFontProgram(fontProgramID, this.fontProgram);
                            }
                        }
                    } else {    // fontFile3
                        ASAtom subtype = type1FontFile.getNameKey(ASAtom.SUBTYPE);
                        boolean isSubset = this.isSubset();
                        if (subtype == ASAtom.TYPE1C) {
                            String fontProgramID = FontProgramIDGenerator.getCFFFontProgramID(key, null, isSubset);
                            this.fontProgram = StaticResources.getCachedFont(fontProgramID);
                            if (fontProgram == null) {
                                try (ASInputStream fontData = type1FontFile.getData(COSStream.FilterFlags.DECODE)) {
                                    this.fontProgram = new CFFFontProgram(fontData, null, isSubset);
                                    StaticResources.cacheFontProgram(fontProgramID, this.fontProgram);
                                }
                            }
                        } else if (subtype == ASAtom.OPEN_TYPE) {
                            boolean isSymbolic = this.isSymbolic();
                            COSObject encoding = this.getEncoding();
                            String fontProgramID = FontProgramIDGenerator.getOpenTypeFontProgramID(key, true, isSymbolic, encoding, null, isSubset);
                            this.fontProgram = StaticResources.getCachedFont(fontProgramID);
                            if (fontProgram == null) {
                                try (ASInputStream fontData = type1FontFile.getData(COSStream.FilterFlags.DECODE)) {
                                    this.fontProgram = new OpenTypeFontProgram(fontData, true, isSymbolic,
                                            encoding, null, isSubset);
                                    StaticResources.cacheFontProgram(fontProgramID, this.fontProgram);
                                }
                            }
                        }
                    }
                } catch (IOException e) {
                    LOGGER.log(Level.FINE, "Can't read Type 1 font program.", e);
                }
            }
        }
    }

    /**
     * @return true if this font is one of standard 14 fonts.
     */
    public Boolean isStandard() {
        if (this.isStandard == null) {
            if (!isEmbedded() && isNameStandard()) {
                isStandard = Boolean.valueOf(true);
                return isStandard;
            }
            isStandard = Boolean.valueOf(false);
            return isStandard;
        }
        return this.isStandard;
    }

    private static String[] getBaseEncoding(COSDictionary encoding) {
        ASAtom baseEncoding = encoding.getNameKey(ASAtom.BASE_ENCODING);
        if (baseEncoding == null) {
            return new String[]{};
        }
        if (baseEncoding == ASAtom.MAC_ROMAN_ENCODING) {
            return Arrays.copyOf(TrueTypePredefined.MAC_ROMAN_ENCODING,
                    TrueTypePredefined.MAC_ROMAN_ENCODING.length);
        } else if (baseEncoding == ASAtom.MAC_EXPERT_ENCODING) {
            return Arrays.copyOf(TrueTypePredefined.MAC_EXPERT_ENCODING,
                    TrueTypePredefined.MAC_EXPERT_ENCODING.length);
        } else if (baseEncoding == ASAtom.WIN_ANSI_ENCODING) {
            return Arrays.copyOf(TrueTypePredefined.WIN_ANSI_ENCODING,
                    TrueTypePredefined.WIN_ANSI_ENCODING.length);
        } else {
            return new String[]{};
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Double getWidth(int code) {
        if (getFontProgram() != null) {
            return super.getWidth(code);
        }
        if (fontMetrics != null) {
            StandardFontMetrics metrics =
                    StandardFontMetricsFactory.getFontMetrics(this.getName());
            Encoding enc = this.getEncodingMapping();
            if (metrics != null) {
                return Double.valueOf(metrics.getWidth(enc.getName(code)));
            }
        }
        // should not get here
        LOGGER.log(Level.FINE, "Can't get standard metrics");
        return null;
    }

    @Override
    public float getWidthFromProgram(int code) {
        Encoding pdEncoding = this.getEncodingMapping();
        String glyphName = pdEncoding.getName(code);
        FontProgram fontProgram = this.getFontProgram();
        return glyphName != null ? fontProgram.getWidth(glyphName) : fontProgram.getWidth(code);
    }

    @Override
    public boolean glyphIsPresent(int code) {
        Encoding pdEncoding = this.getEncodingMapping();
        if (pdEncoding != null) {
            String glyphName = pdEncoding.getName(code);
            if (glyphName != null) {
                return this.getFontProgram().containsGlyph(glyphName);
            }
        }
        return this.getFontProgram().containsCode(code);
    }

    private boolean isEmbedded() {
        return this.getFontProgram() != null;
    }

    private boolean isNameStandard() {
        ASAtom fontName = ASAtom.getASAtom(getName());
        for (ASAtom standard : STANDARD_FONT_NAMES) {
            if (standard == fontName) {
                return true;
            }
        }
        return false;
    }

    /**
     * Get's Unicode value of character as it is described in PDF/A-1
     * specification. The difference from usual toUnicode method is in standard
     * encoding and symbol set lookups for the glyph name.
     *
     * @param code is code of character.
     * @return Unicode value.
     */
    public String toUnicodePDFA1(int code) {
        String unicodeString = super.cMapToUnicode(code);
        if(unicodeString != null) {
            return unicodeString;
        }
        Encoding fontEncoding = this.getEncodingMapping();
        String glyphName =  null;
        if (fontEncoding != null) {
            glyphName = fontEncoding.getName(code);
        }
        if (glyphName == null && getFontProgram() != null) {
            glyphName = fontProgram.getGlyphName(code);
        }
        if (glyphName != null) {
            if (AdobeGlyphList.contains(glyphName) || SymbolSet.hasGlyphName(glyphName)) {
                return " "; // indicates that toUnicode should not be checked.
            }
            return null;
        }
        LOGGER.log(Level.FINE, "Cannot find encoding for glyph with code" + code + " in font " + this.getName());
        return null;
    }

}
