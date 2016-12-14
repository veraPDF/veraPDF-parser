package org.verapdf.pd.font.cff;

import org.verapdf.as.io.ASInputStream;
import org.verapdf.as.io.ASMemoryInStream;
import org.verapdf.pd.font.CFFNumber;
import org.verapdf.pd.font.type1.Type1CharStringParser;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class handles obtaining glyph widths from cff charStrings.
 *
 * @author Sergey Shemyakov
 */
public class CharStringsWidths {

    private static final float DEFAULT_WIDTH = -1f;
    private static final Logger LOGGER = Logger.getLogger(CharStringsWidths.class.getCanonicalName());

    private boolean isSubset;

    private int charStringType;
    private CFFCharStringsHandler charStrings;
    private float[] fontMatrix;
    private boolean isDefaultFontMatrix;

    private CFFIndex localSubrIndex;
    private CFFIndex globalSubrs;
    private int bias;

    private int[] defaultWidths;
    private int[] nominalWidths;
    private int[] fdSelect;

    private float[] subsetFontWidths;
    private Map<Integer, Float> generalFontWidths;

    public CharStringsWidths(boolean isSubset, int charStringType, CFFCharStringsHandler charStrings,
                             float[] fontMatrix, CFFIndex localSubrIndex, CFFIndex globalSubrs,
                             int bias, int[] defaultWidths, int[] nominalWidths, int[] fdSelect) {
        this.isSubset = isSubset;
        this.charStringType = charStringType;
        this.charStrings = charStrings;
        this.fontMatrix = fontMatrix;
        this.isDefaultFontMatrix = Arrays.equals(this.fontMatrix,
                CFFType1FontProgram.DEFAULT_FONT_MATRIX);
        this.localSubrIndex = localSubrIndex;
        this.globalSubrs = globalSubrs;
        this.bias = bias;
        this.defaultWidths = defaultWidths;
        this.nominalWidths = nominalWidths;
        this.fdSelect = fdSelect;
        if (isSubset) {
            parseSubsetWidths();
        } else {
            this.generalFontWidths = new HashMap<>();
        }
    }

    public CharStringsWidths(boolean isSubset, int charStringType, CFFCharStringsHandler charStrings,
                             float[] fontMatrix, CFFIndex localSubrIndex, CFFIndex globalSubrs,
                             int bias, int defaultWidth, int nominalWidth) {
        this(isSubset, charStringType, charStrings, fontMatrix, localSubrIndex,
                globalSubrs, bias, makeArray(defaultWidth), makeArray(nominalWidth), null);
    }

    public float getWidth(int gid) {
        if (isSubset && gid >= 0 && gid < subsetFontWidths.length) {
            return subsetFontWidths[gid];
        } else if (!isSubset) {
            Float res = generalFontWidths.get(gid);
            if (res != null) {
                return res;
            } else {
                CFFNumber width = getWidthFromCharstring(gid);
                res = getActualWidth(width, gid);
                this.generalFontWidths.put(gid, res);
                return res;
            }
        } else {
            LOGGER.log(Level.FINE, "Can't get width of charstring " + gid +
                    " in font subset, got only " + (subsetFontWidths.length - 1) +
                    " charstrings.");
            return DEFAULT_WIDTH;
        }
    }

    public int getWidthsAmount() {
        return this.charStrings.getCharStringAmount();
    }

    private CFFNumber getWidthFromCharstring(int gid) {
        try {
            byte[] charstring = charStrings.getCharString(gid);
            ASInputStream stream = new ASMemoryInStream(charstring, charstring.length, false);
            if (this.charStringType == 1) {
                Type1CharStringParser parser = new Type1CharStringParser(stream);
                return parser.getWidth();
            } else if (this.charStringType == 2) {
                Type2CharStringParser parser = new Type2CharStringParser(stream,
                        localSubrIndex, bias, globalSubrs);
                return parser.getWidth();
            } else {
                throw new IOException("Can't process CharString of type " + this.charStringType);
            }
        } catch (IOException e) {
            return new CFFNumber(-1f);
        }
    }

    private float getActualWidth(CFFNumber charStringWidth, int gid) {
        float res;
        if (charStringWidth == null) {
            res = getDefaultWidth(gid);
        } else {
            res = charStringWidth.isInteger() ? charStringWidth.getInteger() :
                    charStringWidth.getReal();
            res += getNominalWidth(gid);
        }
        if (!isDefaultFontMatrix) {
            res *= (fontMatrix[0] * 1000);
        }
        return res;
    }

    private int getDefaultWidth(int gid) {
        return getPredefinedWidth(gid, this.defaultWidths);
    }

    private int getNominalWidth(int gid) {
        return getPredefinedWidth(gid, this.nominalWidths);
    }

    private int getPredefinedWidth(int gid, int[] widthArray) {
        if (this.fdSelect == null) {
            return widthArray[0];
        } else {
            return widthArray[this.fdSelect[gid]];
        }
    }

    private void parseSubsetWidths() {
        this.subsetFontWidths = new float[this.charStrings.getCharStringAmount()];
        for (int i = 0; i < subsetFontWidths.length; ++i) {
            CFFNumber width = getWidthFromCharstring(i);
            this.subsetFontWidths[i] = getActualWidth(width, i);
        }
    }

    private static int[] makeArray(int num) {
        int[] res = new int[1];
        res[0] = num;
        return res;
    }
}
