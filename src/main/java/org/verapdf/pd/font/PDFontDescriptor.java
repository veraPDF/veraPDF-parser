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
import org.verapdf.cos.COSBase;
import org.verapdf.cos.COSObjType;
import org.verapdf.cos.COSObject;
import org.verapdf.cos.COSStream;
import org.verapdf.pd.PDObject;
import org.verapdf.pd.font.stdmetrics.StandardFontMetrics;

import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Represents font descriptor.
 *
 * @author Sergey Shemyakov
 */
public class PDFontDescriptor extends PDObject {

    private static final Logger LOGGER =
            Logger.getLogger(PDFontDescriptor.class.getCanonicalName());
    private final Long flags;

    private static final int FIXED_PITCH_BIT = 1;
    private static final int SERIF_BIT = 2;
    private static final int SYMBOLIC_BIT = 3;
    private static final int SCRIPT_BIT = 4;
    private static final int NONSYMBOLIC_BIT = 6;
    private static final int ITALIC_BIT = 7;
    private static final int ALL_CAP_BIT = 17;
    private static final int SMALL_CAP_BIT = 18;
    private static final int FORCE_BOLD_BIT = 19;
    private static final Double DEFAULT_LEADING = new Double(0);
    private static final Double DEFAULT_XHEIGHT = new Double(0);
    private static final Double DEFAULT_STEM_H = new Double(0);
    private static final Double DEFAULT_WIDTH = new Double(0);

    // values
    private String fontName;
    private String fontFamily;
    private ASAtom fontStretch;
    private Double fontWeight;
    private Boolean isFixedPitch;
    private Boolean isSerif;
    private Boolean isSymbolic;
    private Boolean isScript;
    private Boolean isNonSymblic;
    private Boolean isItalic;
    private Boolean isAllCap;
    private Boolean isSmallCup;
    private Boolean isForceBold;
    private double[] fontBoundingBox;
    private Double italicAngle;
    private Double ascent;
    private Double descent;
    private Double leading;
    private Double capHeight;
    private Double xHeight;
    private Double stemV;
    private Double stemH;
    private Double avgWidth;
    private Double maxWidth;
    private Double missingWidth;
    private String charSet;

    /**
     * Constructor from font descriptor COSObject.
     * @param obj is descriptor COSObject.
     */
    public PDFontDescriptor(COSObject obj) {
        super(obj);
        flags = getFlags();
        fontName = (getFontName() == null ? "" : getFontName().getValue());
        if (flags == null) {
            LOGGER.log(Level.FINE, "Font descriptor for font " +
                    fontName + " doesn't contain flags.");
        }
    }

    /**
     * @return a collection of flags defining various characteristics of the
     * font.
     */
    public Long getFlags() {
        return getIntegerKey(ASAtom.FLAGS);
    }

    /**
     * @return the PostScript name of the font.
     */
    public ASAtom getFontName() {
        return getNameKey(ASAtom.FONT_NAME);
    }

    /**
     * @return a byte string specifying the preferred font family name.
     */
    public String getFontFamily() {
        if (fontFamily == null) {
            fontFamily = getStringKey(ASAtom.FONT_FAMILY);
        }
        return fontFamily;
    }

    /**
     * @return the font stretch value.
     */
    public ASAtom getFontStretch() {
        if (fontStretch == null) {
            fontStretch = getNameKey(ASAtom.FONT_STRETCH);
        }
        return fontStretch;
    }

    /**
     * @return the weight (thickness) component of the fully-qualified font name
     * or font specifier.
     */
    public Double getFontWeight() {
        if (fontWeight == null) {
            fontWeight = getRealKey(ASAtom.FONT_WEIGHT);
        }
        return fontWeight;
    }

    /**
     * @return true if all glyphs have the same width.
     */
    public boolean isFixedPitch() {
        if (isFixedPitch == null) {
            isFixedPitch = isFlagBitOn(FIXED_PITCH_BIT);
        }
        return isFixedPitch;
    }

    /**
     * @return true if glyphs have serifs, which are short strokes drawn at an
     * angle on the top and bottom of glyph stems.
     */
    public boolean isSerif() {
        if (isSerif == null) {
            isSerif = isFlagBitOn(SERIF_BIT);
        }
        return isSerif;
    }

    /**
     * @return true if font contains glyphs outside the Adobe standard Latin
     * character set.
     */
    public boolean isSymbolic() {
        if (isSymbolic == null) {
            isSymbolic = isFlagBitOn(SYMBOLIC_BIT);
        }
        return isSymbolic;
    }

    /**
     * @return true if glyphs resemble cursive handwriting.
     */
    public boolean isScript() {
        if (isScript == null) {
            isScript = isFlagBitOn(SCRIPT_BIT);
        }
        return isScript;
    }

    /**
     * @return true if font uses the Adobe standard Latin character set or a
     * subset of it.
     */
    public boolean isNonsymbolic() {
        if (isNonSymblic == null) {
            isNonSymblic = isFlagBitOn(NONSYMBOLIC_BIT);
        }
        return isNonSymblic;
    }

    /**
     * @return true if glyphs have dominant vertical strokes that are slanted.
     */
    public boolean isItalic() {
        if (isItalic == null) {
            isItalic = isFlagBitOn(ITALIC_BIT);
        }
        return isItalic;
    }

    /**
     * @return true if font contains no lowercase letters; typically used for
     * display purposes, such as for titles or headlines.
     */
    public boolean isAllCap() {
        if (isAllCap == null) {
            isAllCap = isFlagBitOn(ALL_CAP_BIT);
        }
        return isAllCap;
    }

    /**
     * @return true if font contains both uppercase and lowercase letters.
     */
    public boolean isSmallCap() {
        if (isSmallCup == null) {
            isSmallCup = isFlagBitOn(SMALL_CAP_BIT);
        }
        return isSmallCup;
    }

    /**
     * @return true if bold glyphs shall be painted with extra pixels even at
     * very small text sizes by a conforming reader.
     */
    public boolean isForceBold() {
        if (isForceBold == null) {
            isForceBold = isFlagBitOn(FORCE_BOLD_BIT);
        }
        return isForceBold;
    }

    private boolean isFlagBitOn(int bit) {
        return flags != null && (flags.intValue() & (1 << (bit - 1))) != 0;
    }

    /**
     * @return a rectangle, expressed in the glyph coordinate system, that shall
     * specify the font bounding box.
     */
    public double[] getFontBoundingBox() {
        if (fontBoundingBox == null) {
            COSBase bbox = this.getObject().getKey(ASAtom.FONT_BBOX).get();
            if (bbox != null && bbox.getType() == COSObjType.COS_ARRAY && bbox.size() == 4) {
                double[] res = new double[4];
                for (int i = 0; i < 4; ++i) {
                    COSObject obj = bbox.at(i);
                    if (obj.getType().isNumber()) {
                        res[i] = obj.getReal();
                    } else {
                        LOGGER.log(Level.SEVERE, "Font bounding box array for font " + fontName +
                                " contains " + obj.getType());
                        return null;
                    }
                }
                fontBoundingBox = res;
            } else {
                LOGGER.log(Level.SEVERE, "Font bounding box array for font " + fontName +
                        " is not an array of 4 elements");
                return null;
            }
        }
        return fontBoundingBox;
    }

    /**
     * @return the angle, expressed in degrees counterclockwise from the
     * vertical, of the dominant vertical strokes of the font.
     */
    public Double getItalicAngle() {
        if (italicAngle == null) {
            italicAngle = getRealKey(ASAtom.ITALIC_ANGLE);
        }
        return italicAngle;
    }

    /**
     * @return the maximum height above the baseline reached by glyphs in this
     * font.
     */
    public Double getAscent() {
        if (ascent == null) {
            ascent = getRealKey(ASAtom.ASCENT);
        }
        return ascent;
    }

    /**
     * @return the maximum depth below the baseline reached by glyphs in this
     * font.
     */
    public Double getDescent() {
        if (descent == null) {
            descent = getRealKey(ASAtom.DESCENT);
        }
        return descent;
    }

    /**
     * @return the spacing between baselines of consecutive lines of text.
     */
    public Double getLeading() {
        if (leading == null) {
            Double res = getRealKey(ASAtom.LEADING);
            leading = res == null ? DEFAULT_LEADING : res;
        }
        return leading;
    }

    /**
     * @return the vertical coordinate of the top of flat capital letters,
     * measured from the baseline.
     */
    public Double getCapHeight() {
        if (capHeight == null) {
            capHeight = getRealKey(ASAtom.CAP_HEIGHT);
        }
        return capHeight;
    }

    /**
     * @return the font’s x height: the vertical coordinate of the top of flat
     * nonascending lowercase letters (like the letter x), measured from the
     * baseline, in fonts that have Latin characters.
     */
    public Double getXHeight() {
        if (xHeight == null) {
            Double res = getRealKey(ASAtom.XHEIGHT);
            xHeight = res == null ? DEFAULT_XHEIGHT : res;
        }
        return xHeight;
    }

    /**
     * @return the thickness, measured horizontally, of the dominant vertical
     * stems of glyphs in the font.
     */
    public Double getStemV() {
        if (stemV == null) {
            stemV = getRealKey(ASAtom.STEM_V);
        }
        return stemV;
    }

    /**
     * @return the thickness, measured vertically, of the dominant horizontal
     * stems of glyphs in the font.
     */
    public Double getStemH() {
        if (stemH == null) {
            Double res = getRealKey(ASAtom.STEM_H);
            stemH = res == null ? DEFAULT_STEM_H : res;
        }
        return stemH;
    }

    /**
     * @return the average width of glyphs in the font.
     */
    public Double getAvgWidth() {
        if (avgWidth == null) {
            Double res = getRealKey(ASAtom.AVG_WIDTH);
            avgWidth = res == null ? DEFAULT_WIDTH : res;
        }
        return avgWidth;
    }

    /**
     * @return the maximum width of glyphs in the font.
     */
    public Double getMaxWidth() {
        if (maxWidth == null) {
            Double res = getRealKey(ASAtom.MAX_WIDTH);
            maxWidth = res == null ? DEFAULT_WIDTH : res;
        }
        return maxWidth;
    }

    /**
     * @return the width to use for character codes whose widths are not
     * specified in a font dictionary’s Widths array.
     */
    public Double getMissingWidth() {
        if (missingWidth == null) {
            Double res = getRealKey(ASAtom.MISSING_WIDTH);
            missingWidth = res == null ? DEFAULT_WIDTH : res;
        }
        return missingWidth;
    }

    /**
     * @return a string listing the character names defined in a font subset.
     */
    public String getCharSet() {
        if (charSet == null) {
            charSet = getStringKey(ASAtom.CHAR_SET);
        }
        return charSet;
    }

    /**
     * @return a stream containing a Type 1 font program.
     */
    public COSStream getFontFile() {
        return getCOSStreamWithCheck(ASAtom.FONT_FILE);
    }

    /**
     * @return a stream containing a TrueType font program.
     */
    public COSStream getFontFile2() {
        return getCOSStreamWithCheck(ASAtom.FONT_FILE2);
    }

    /**
     * @return a stream containing a font program whose format is specified by
     * the Subtype entry in the stream dictionary.
     */
    public COSStream getFontFile3() {
        return getCOSStreamWithCheck(ASAtom.FONT_FILE3);
    }

    /**
     * Checks if specific font program can be read from this font descriptor.
     *
     * @param key is key of font program in font descriptor dictionary, e.g.
     *            FontFile or FontFile2.
     * @return true if value with this key exists and is a COSStream.
     */
    public boolean canParseFontFile(ASAtom key) {
        return this.knownKey(key) && getCOSStreamWithCheck(key) != null;
    }

    private COSStream getCOSStreamWithCheck(ASAtom key) {
        COSObject res = getKey(key);
        if (res.getType() == COSObjType.COS_STREAM) {
            return (COSStream) res.getDirectBase();
        } else {
            return null;
        }
    }

    public void setFontName(ASAtom fontName) {
        if (fontName != null) {
            this.fontName = fontName.getValue();
            this.setNameKey(ASAtom.FONT_NAME, fontName);
        }
    }

    public void setFontFamily(String fontFamily) {
        this.fontFamily = fontFamily;
    }

    public void setFontStretch(ASAtom fontStretch) {
        this.fontStretch = fontStretch;
    }

    public void setFontWeight(Double fontWeight) {
        this.fontWeight = fontWeight;
    }

    public void setFixedPitch(Boolean fixedPitch) {
        isFixedPitch = fixedPitch;
    }

    public void setSerif(Boolean serif) {
        isSerif = serif;
    }

    public void setSymbolic(Boolean symbolic) {
        isSymbolic = symbolic;
    }

    public void setScript(Boolean script) {
        isScript = script;
    }

    public void setNonSymblic(Boolean nonSymblic) {
        isNonSymblic = nonSymblic;
    }

    public void setItalic(Boolean italic) {
        isItalic = italic;
    }

    public void setAllCap(Boolean allCap) {
        isAllCap = allCap;
    }

    public void setSmallCup(Boolean smallCup) {
        isSmallCup = smallCup;
    }

    public void setForceBold(Boolean forceBold) {
        isForceBold = forceBold;
    }

    public void setFontBoundingBox(double[] fontBoundingBox) {
        this.fontBoundingBox = fontBoundingBox;
    }

    public void setItalicAngle(Double italicAngle) {
        this.italicAngle = italicAngle;
    }

    public void setAscent(Double ascent) {
        this.ascent = ascent;
    }

    public void setDescent(Double descent) {
        this.descent = descent;
    }

    public void setLeading(Double leading) {
        this.leading = leading;
    }

    public void setCapHeight(Double capHeight) {
        this.capHeight = capHeight;
    }

    public void setxHeight(Double xHeight) {
        this.xHeight = xHeight;
    }

    public void setStemV(Double stemV) {
        this.stemV = stemV;
    }

    public void setStemH(Double stemH) {
        this.stemH = stemH;
    }

    public void setAvgWidth(Double avgWidth) {
        this.avgWidth = avgWidth;
    }

    public void setMaxWidth(Double maxWidth) {
        this.maxWidth = maxWidth;
    }

    public void setMissingWidth(Double missingWidth) {
        this.missingWidth = missingWidth;
    }

    public void setCharSet(String charSet) {
        this.charSet = charSet;
    }

    /**
     * Constructs font descriptor for given standard font metrics.
     *
     * @param sfm is standard font metrics.
     * @return font descriptor with fields set in accordance with sfm.
     */
    public static PDFontDescriptor getDescriptorFromMetrics(StandardFontMetrics sfm) {
        PDFontDescriptor res = new PDFontDescriptor(new COSObject());
        boolean isSymbolic = "FontSpecific".equals(sfm.getEncodingScheme());
        res.fontName = sfm.getFontName();
        res.setFontName(ASAtom.getASAtom(sfm.getFontName()));
        res.fontFamily = sfm.getFamilyName();
        res.fontBoundingBox = sfm.getFontBBox();
        res.isSymbolic = isSymbolic;
        res.isNonSymblic = !isSymbolic;
        res.charSet = sfm.getCharSet();
        res.capHeight = sfm.getCapHeight();
        res.xHeight = sfm.getXHeight();
        res.descent = sfm.getDescend();
        res.ascent = sfm.getAscend();
        res.italicAngle = sfm.getItalicAngle();

        double totalWidth = 0;
        int glyphNum = 0;
        Iterator<Map.Entry<String, Integer>> widthsIterator = sfm.getWidthsIterator();
        while(widthsIterator.hasNext()) {
            Integer width = widthsIterator.next().getValue();
            if (width != null && width > 0) {
                totalWidth += width;
                glyphNum++;
            }
        }
        if (glyphNum != 0) {
            res.avgWidth = totalWidth / glyphNum;
        }
        return res;
    }
}
