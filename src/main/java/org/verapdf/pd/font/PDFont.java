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
package org.verapdf.pd.font;

import org.verapdf.as.ASAtom;
import org.verapdf.cos.*;
import org.verapdf.pd.PDResource;
import org.verapdf.pd.font.cmap.PDCMap;
import org.verapdf.pd.font.type1.PDType1Font;
import org.verapdf.pd.font.type3.PDType3Font;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This is PD representation of font.
 *
 * @author Sergey Shemyakov
 */
public abstract class PDFont extends PDResource {

    private static final Map<String, Double> weightNames = new LinkedHashMap<>();

    private static final Logger LOGGER = Logger.getLogger(PDFont.class.getCanonicalName());

    protected COSDictionary dictionary;
    protected PDFontDescriptor fontDescriptor;
    protected PDCMap toUnicodeCMap;
    protected boolean isFontParsed = false;
    protected FontProgram fontProgram;
    protected Encoding encoding = null;
    private boolean successfullyParsed = false;
    private final String fontName;
    private final String fontNameWithoutSubset;
    private final ASAtom subtype;
    protected double[] boundingBox;
    private Double fontWeight;

    /**
     * Constructor from COSDictionary.
     *
     * @param dictionary is font dictionary.
     */
    public PDFont(COSDictionary dictionary) {
        super(new COSObject(dictionary));
        if (dictionary == null) {
            dictionary = (COSDictionary) COSDictionary.construct().get();
        }
        this.dictionary = dictionary;
        COSObject fd = dictionary.getKey(ASAtom.FONT_DESC);
        if (fd != null && fd.getType() == COSObjType.COS_DICT) {
            fontDescriptor = new PDFontDescriptor(fd);
        } else {
            fontDescriptor = new PDFontDescriptor(null);
        }
        this.fontName = this.dictionary.getNameKeyUnicodeValue(ASAtom.BASE_FONT);
        this.fontNameWithoutSubset = fontName != null ? (fontName.matches("^[A-Z]{6}+.+") ? fontName.substring(7) : fontName) : null;
        if (!(this instanceof PDType3Font)) {
            this.boundingBox = fontDescriptor.getFontBoundingBox();
            if (this.boundingBox == null) {
                this.boundingBox = new double[]{0.0, 0.0, 1000.0, 1000.0};
            }
        }
        this.subtype = this.dictionary.getNameKey(ASAtom.SUBTYPE);
    }

    /**
     * @return font COSDictionary.
     */
    public COSDictionary getDictionary() {
        return dictionary;
    }

    /**
     * @return font descriptor COSDictionary.
     */
    public PDFontDescriptor getFontDescriptor() {
        return fontDescriptor;
    }

    /**
     * @return font type (Type entry).
     */
    public String getType() {
        String type = this.dictionary.getNameKeyStringValue(ASAtom.TYPE);
        return type == null ? "" : type;
    }

    /**
     * @return font subtype (Subtype entry).
     */
    public ASAtom getSubtype() {
        return this.subtype;
    }
    
    public ASAtom getSubtypeEntryValue() {
        return this.subtype;
    }

    /**
     * @return true if the font flags in the font descriptor dictionary mark
     * indicate that the font is symbolic (the entry /Flags has bit 3 set to 1
     * and bit 6 set to 0).
     * descriptor is null.
     */
    public boolean isSymbolic() {
        return this.fontDescriptor.isSymbolic();
    }

    /**
     * @return encoding mapping object for this font.
     */
    public Encoding getEncodingMapping() {
        if (this.encoding == null) {
            this.encoding = calculateEncodingMapping();
        }
        return this.encoding;
    }
    
    protected Encoding calculateEncodingMapping() {
        return getEncodingMappingFromCOSObject(this.getEncoding());
    }

    public Double getFontWeight() {
        if (fontWeight == null) {
            this.fontWeight = fontDescriptor.getFontWeight();
            if (fontWeight == null) {
                detectFontWeight();
            }
        }
        return fontWeight;
    }

    /**
     * Gets encoding object from COSObject.
     *
     * @param e is value of Encoding key in font dictionary.
     * @return encoding object for given COSObject.
     */
    private static Encoding getEncodingMappingFromCOSObject(COSObject e) {
        COSBase cosEncoding = e.getDirectBase();
        if (cosEncoding != null) {
            Encoding encodingObj;
            if (cosEncoding.getType() == COSObjType.COS_NAME) {
                encodingObj = new Encoding(cosEncoding.getName());
                return encodingObj;
            } else if (cosEncoding.getType() == COSObjType.COS_DICT) {
                encodingObj = new Encoding(cosEncoding.getNameKey(ASAtom.BASE_ENCODING),
                        getDifferencesFromCosEncoding(e));
                return encodingObj;
            }
        }
        return Encoding.empty();
    }

    /**
     * @return name of the font as specified in BaseFont key of font dictionary.
     */
    public String getName() {
        return this.fontName;
    }

    /**
     * @return name of the font without subset as specified in BaseFont key of font dictionary.
     */
    public String getNameWithoutSubset() {
        return this.fontNameWithoutSubset;
    }

    public double[] getBoundingBox() {
        return boundingBox;
    }

    /**
     * @return encoding of the font as specified in Encoding key of font
     * dictionary.
     */
    public COSObject getEncoding() {
        return this.dictionary.getKey(ASAtom.ENCODING);
    }

    /**
     * @return map of differences as given in Differences key in Encoding of
     * this font.
     */
    public Map<Integer, String> getDifferences() {
        return getDifferencesFromCosEncoding(this.getEncoding());
    }

    /**
     * @return map of differences as given in Differences key in Encoding.
     */
    public static Map<Integer, String> getDifferencesFromCosEncoding(COSObject e) {
        COSObject cosDifferences = e.getKey(ASAtom.DIFFERENCES);
        if (cosDifferences == null) {
            return Collections.emptyMap();
        }
        COSArray differences;
        if (cosDifferences.getType() == COSObjType.COS_ARRAY) {
            differences = (COSArray) cosDifferences.getDirectBase();
        } else {
            if (!cosDifferences.empty()) {
                LOGGER.log(Level.SEVERE, "Value of Differences key is not an array. Ignoring Difference");
            }
            differences = null;
        }
        if (differences == null) {
            return Collections.emptyMap();
        }
        Map<Integer, String> res = new HashMap<>();
        int diffIndex = 0;
        for (COSObject obj : differences) {
            if (obj.getType() == COSObjType.COS_INTEGER) {
                diffIndex = obj.getInteger().intValue();
            } else if (obj.getType() == COSObjType.COS_NAME && diffIndex != -1) {
                res.put(diffIndex++, obj.getString());
            }
        }
        return res;
    }

    /**
     * @return widths of the font as specified in Widths key of font dictionary.
     */
    public COSObject getWidths() {
        return this.dictionary.getKey(ASAtom.WIDTHS);
    }

    /**
     * @return first char in the font as specified in FirstChar key of font
     * dictionary.
     */
    public Long getFirstChar() {
        return this.dictionary.getIntegerKey(ASAtom.FIRST_CHAR);
    }

    /**
     * @return last char in the font as specified in LastChar key of font
     * dictionary.
     */
    public Long getLastChar() {
        return this.dictionary.getIntegerKey(ASAtom.LAST_CHAR);
    }

    /**
     * Method reads next character code from stream according to font data. It
     * can contain from 1 to 4 bytes.
     *
     * @param stream is stream with raw data.
     * @return next character code read.
     * @throws IOException if reading fails.
     */
    public int readCode(InputStream stream) throws IOException {
        return stream.read();
    }

    /**
     * @return embedded font program fo this PDFont.
     */
    public abstract FontProgram getFontProgram();

    /**
     * Gets width of given code from font program.
     *
     * @param code is code of character in strings to display.
     * @return width of glyph for this code.
     */
    public abstract float getWidthFromProgram(int code);

    /**
     * Checks if glyph for given code is present in this font.
     *
     * @param code is code for glyph in this font.
     * @return true if glyph is present.
     */
    public abstract boolean glyphIsPresent(int code);

    /**
     * Gets Unicode string for given character code. This method returns null in
     * case when no toUnicode mapping for this character was found, so some
     * inherited classes need to call this method, check return value on null
     * and then implement their special logic.
     *
     * @param code is code for character.
     * @return Unicode string
     */
    public String toUnicode(int code) {
        return cMapToUnicode(code);
    }

    /**
     * Gets toUnicode value just from toUnicode cMap.
     *
     * @param code is character code.
     * @return Unicode value as specified in toUnicode cMap.
     */
    public String cMapToUnicode(int code) {
        if (toUnicodeCMap == null) {
            this.toUnicodeCMap = new PDCMap(this.dictionary.getKey(ASAtom.TO_UNICODE));
        }
        if (toUnicodeCMap.getCMapName() != null && toUnicodeCMap.isIdentity()) {
            return new String(new char[]{(char) code});
        }
        return this.toUnicodeCMap.toUnicode(code);
    }

    /**
     * @return value of Subtype key in embedded font program stream or null if
     * no value available.
     */
    public ASAtom getProgramSubtype() {
        COSStream fontFile = fontDescriptor.getFontFile();
        if (fontFile == null) {
            fontFile = fontDescriptor.getFontFile2();
            if (fontFile == null) {
                fontFile = fontDescriptor.getFontFile3();
            }
        }
        return fontFile == null ? null : fontFile.getNameKey(ASAtom.SUBTYPE);
    }

    /**
     * Gets width for glyph with given code in this font.
     *
     * @param code is code of glyph.
     * @return width for glyph with given code as specified in Widths array.
     */
    public Double getWidth(int code) {
        if (dictionary.knownKey(ASAtom.WIDTHS) && dictionary.knownKey(ASAtom.FIRST_CHAR)
                && dictionary.knownKey(ASAtom.LAST_CHAR)) {
            int firstChar = dictionary.getIntegerKey(ASAtom.FIRST_CHAR).intValue();
            int lastChar = dictionary.getIntegerKey(ASAtom.LAST_CHAR).intValue();
            COSObject widths = getWidths();
            if (widths.getType() == COSObjType.COS_ARRAY && widths.size() > 0 && code >= firstChar && code <= lastChar) {
                return widths.at(code - firstChar).getReal();
            }
        }

        if (fontDescriptor.knownKey(ASAtom.MISSING_WIDTH)) {
            return fontDescriptor.getMissingWidth();
        }

        if (this instanceof PDType3Font || this instanceof PDType1Font) {
            return null;
        }

        return (double) 0;
    }

    /**
     * @return default width for this font as specified in font descriptor.
     */
    public Double getDefaultWidth() {
        return fontDescriptor.getMissingWidth();
    }

    /**
     * @return true if font program for this font has been successfully parsed.
     */
    public boolean isSuccessfullyParsed() {
        return successfullyParsed;
    }

    /**
     * Sets flag indicating successful parsing of embedded font program.
     */
    public void setSuccessfullyParsed(boolean successfullyParsed) {
        this.successfullyParsed = successfullyParsed;
    }

    protected boolean isSubset() {
        String[] nameSplitting = this.getName().split("\\+");
        return nameSplitting[0].length() == 6;
    }

    public Double getAscent() {
        FontProgram program = this.getFontProgram();
        return program != null ? program.getAscent() : null;
    }

    public Double getDescent() {
        FontProgram program = this.getFontProgram();
        return program != null ? program.getDescent() : null;
    }

    private void detectFontWeight() {
        FontProgram program = this.getFontProgram();
        if (program != null) {
            this.fontWeight = weightNames.get(program.getWeight());
        }
        if (this.fontWeight == null) {
            if (fontNameWithoutSubset != null) {
                for (Map.Entry<String, Double> entry : weightNames.entrySet()) {
                    if (fontNameWithoutSubset.contains(entry.getKey())) {
                        fontWeight = entry.getValue();
                        return;
                    }
                }
            }
            fontWeight = 400.0;
        }
    }

    static {
        weightNames.put("Thin", 100.0);
        weightNames.put("ExtraLight", 200.0);
        weightNames.put("Extra Light", 200.0);
        weightNames.put("Extra-Light", 200.0);
        weightNames.put("UltraLight", 200.0);
        weightNames.put("Ultra Light", 200.0);
        weightNames.put("Ultra-Light", 200.0);
        weightNames.put("Light", 300.0);
        weightNames.put("Normal", 400.0);
        weightNames.put("Book", 400.0);
        weightNames.put("Regular", 400.0);
        weightNames.put("Medium", 500.0);
        weightNames.put("Semibold", 600.0);
        weightNames.put("SemiBold", 600.0);
        weightNames.put("Semi Bold", 600.0);
        weightNames.put("Semi-Bold", 600.0);
        weightNames.put("DemiBold", 600.0);
        weightNames.put("Demi Bold", 600.0);
        weightNames.put("Demi-Bold", 600.0);
        weightNames.put("ExtraBold", 800.0);
        weightNames.put("Extra Bold", 800.0);
        weightNames.put("Extra-Bold", 800.0);
        weightNames.put("UltraBold", 800.0);
        weightNames.put("Ultra Bold", 800.0);
        weightNames.put("Ultra-Bold", 800.0);
        weightNames.put("Bold", 700.0);
        weightNames.put("Black", 900.0);
        weightNames.put("Heavy", 900.0);
    }

}
