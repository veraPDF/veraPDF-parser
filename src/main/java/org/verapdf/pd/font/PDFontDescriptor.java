package org.verapdf.pd.font;

import org.verapdf.as.ASAtom;
import org.verapdf.cos.COSBase;
import org.verapdf.cos.COSObjType;
import org.verapdf.cos.COSObject;
import org.verapdf.cos.COSStream;
import org.verapdf.pd.PDObject;

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
    private final String fontName;

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
        return getStringKey(ASAtom.FONT_FAMILY);
    }

    /**
     * @return the font stretch value.
     */
    public ASAtom getFontStretch() {
        return getNameKey(ASAtom.FONT_STRETCH);
    }

    /**
     * @return the weight (thickness) component of the fully-qualified font name
     * or font specifier.
     */
    public Double getFontWeight() {
        return getRealKey(ASAtom.FONT_WEIGHT);
    }

    /**
     * @return true if all glyphs have the same width.
     */
    public boolean isFixedPitch() {
        return isFlagBitOn(FIXED_PITCH_BIT);
    }

    /**
     * @return true if glyphs have serifs, which are short strokes drawn at an
     * angle on the top and bottom of glyph stems.
     */
    public boolean isSerif() {
        return isFlagBitOn(SERIF_BIT);
    }

    /**
     * @return true if font contains glyphs outside the Adobe standard Latin
     * character set.
     */
    public boolean isSymbolic() {
        return isFlagBitOn(SYMBOLIC_BIT);
    }

    /**
     * @return true if glyphs resemble cursive handwriting.
     */
    public boolean isScript() {
        return isFlagBitOn(SCRIPT_BIT);
    }

    /**
     * @return true if font uses the Adobe standard Latin character set or a
     * subset of it.
     */
    public boolean isNonsymbolic() {
        return isFlagBitOn(NONSYMBOLIC_BIT);
    }

    /**
     * @return true if glyphs have dominant vertical strokes that are slanted.
     */
    public boolean isItalic() {
        return isFlagBitOn(ITALIC_BIT);
    }

    /**
     * @return true if font contains no lowercase letters; typically used for
     * display purposes, such as for titles or headlines.
     */
    public boolean isAllCap() {
        return isFlagBitOn(ALL_CAP_BIT);
    }

    /**
     * @return true if font contains both uppercase and lowercase letters.
     */
    public boolean isSmallCap() {
        return isFlagBitOn(SMALL_CAP_BIT);
    }

    /**
     * @return true if bold glyphs shall be painted with extra pixels even at
     * very small text sizes by a conforming reader.
     */
    public boolean isForceBold() {
        return isFlagBitOn(FORCE_BOLD_BIT);
    }

    private boolean isFlagBitOn(int bit) {
        if (flags != null) {
            return (flags.intValue() & (1 << (bit - 1))) != 0;
        } else {
            return false;
        }
    }

    /**
     * @return a rectangle, expressed in the glyph coordinate system, that shall
     * specify the font bounding box.
     */
    public double[] getFontBoundingBox() {
        COSBase bbox = this.getObject().getKey(ASAtom.FONT_BBOX).get();
        if (bbox.getType() == COSObjType.COS_ARRAY || bbox.size() == 4) {
            double[] res = new double[4];
            for (int i = 0; i < 4; ++i) {
                COSObject obj = bbox.at(i);
                if (obj.getType().isNumber()) {
                    res[i] = obj.getReal();
                } else {
                    LOGGER.log(Level.FINE, "Font bounding box array for font " + fontName +
                    " contains " + obj.getType());
                    return null;
                }
            }
            return res;
        } else {
            LOGGER.log(Level.FINE, "Font bounding box array for font " + fontName +
            " is not an array of 4 elements");
            return null;
        }
    }

    /**
     * @return the angle, expressed in degrees counterclockwise from the
     * vertical, of the dominant vertical strokes of the font.
     */
    public Double getItalicAngle() {
        return getRealKey(ASAtom.ITALIC_ANGLE);
    }

    /**
     * @return the maximum height above the baseline reached by glyphs in this
     * font.
     */
    public Double getAscent() {
        return getRealKey(ASAtom.ASCENT);
    }

    /**
     * @return the maximum depth below the baseline reached by glyphs in this
     * font.
     */
    public Double getDescent() {
        return getRealKey(ASAtom.DESCENT);
    }

    /**
     * @return the spacing between baselines of consecutive lines of text.
     */
    public Double getLeading() {
        Double res = getRealKey(ASAtom.LEADING);
        return res == null ? DEFAULT_LEADING : res;
    }

    /**
     * @return the vertical coordinate of the top of flat capital letters,
     * measured from the baseline.
     */
    public Double getCapHeight() {
        return getRealKey(ASAtom.CAP_HEIGHT);
    }

    /**
     * @return the font’s x height: the vertical coordinate of the top of flat
     * nonascending lowercase letters (like the letter x), measured from the
     * baseline, in fonts that have Latin characters.
     */
    public Double getXHeight() {
        Double res = getRealKey(ASAtom.XHEIGHT);
        return res == null ? DEFAULT_XHEIGHT : res;
    }

    /**
     * @return the thickness, measured horizontally, of the dominant vertical
     * stems of glyphs in the font.
     */
    public Double getStemV() {
        return getRealKey(ASAtom.STEM_V);
    }

    /**
         * @return the thickness, measured vertically, of the dominant horizontal
         * stems of glyphs in the font.
     */
    public Double getStemH() {
        Double res = getRealKey(ASAtom.STEM_H);
        return res == null ? DEFAULT_STEM_H : res;
    }

    /**
     * @return the average width of glyphs in the font.
     */
    public Double getAvgWidth() {
        Double res = getRealKey(ASAtom.AVG_WIDTH);
        return res == null ? DEFAULT_WIDTH : res;
    }

    /**
     * @return the maximum width of glyphs in the font.
     */
    public Double getMaxWidth() {
        Double res = getRealKey(ASAtom.MAX_WIDTH);
        return res == null ? DEFAULT_WIDTH : res;
    }

    /**
     * @return the width to use for character codes whose widths are not
     * specified in a font dictionary’s Widths array.
     */
    public Double getMissingWidth() {
        Double res = getRealKey(ASAtom.MISSING_WIDTH);
        return res == null ? DEFAULT_WIDTH : res;
    }

    /**
     * @return a string listing the character names defined in a font subset.
     */
    public String getCharSet() {
        return getStringKey(ASAtom.CHAR_SET);
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

    private COSStream getCOSStreamWithCheck(ASAtom key) {
        COSObject res = getKey(key);
        if (res.getType() == COSObjType.COS_STREAM) {
            return (COSStream) res.get();
        } else {
            return null;
        }
    }
}
