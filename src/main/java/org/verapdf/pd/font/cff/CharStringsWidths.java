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
 * This class handles obtaining glyph widths from cff charStrings. If font is
 * a subset, then all charstrings are parsed at initialization, else each
 * charstring is parsed separately in getter.
 *
 * @author Sergey Shemyakov
 */
public class CharStringsWidths {

    private static final float DEFAULT_WIDTH = -1f;
    private static final Logger LOGGER = Logger.getLogger(CharStringsWidths.class.getCanonicalName());

    private boolean isSubset;

    private int charStringType;
    private CFFCharStringsHandler charStrings;
    private float[][] fontMatrices;
    private boolean[] isDefaultFontMatrices;
    
    private CFFIndex globalSubrs;
    
    private CFFIndex[] localSubrIndexes;
    private int[] bias;
    private int[] defaultWidths;
    private int[] nominalWidths;
    private int[] fdSelect;

    private float[] subsetFontWidths;
    private Map<Integer, Float> generalFontWidths;

    /**
     * Initializes handler with given values.
     *
     * @param isSubset is true if font is subset. In this case all widths will
     *                 be parsed during handler initialization.
     * @param charStringType is type of charstring.
     * @param charStrings is charstring handler with charstring data.
     * @param fontMatrices is array of font matrix for each FDArray element.
     * @param localSubrIndex is array of local subrs for each FDArray element.
     * @param globalSubrs is array of global subrs for each FDArray element.
     * @param bias is array of bias values for each FDArray element.
     * @param defaultWidths is array of default widths for each FDArray element.
     * @param nominalWidths is array of nominal widths for each FDArray element.
     * @param fdSelect is fd select array as specified in CFF font.
     */
    public CharStringsWidths(boolean isSubset, int charStringType, CFFCharStringsHandler charStrings,
                             float[][] fontMatrices, CFFIndex[] localSubrIndex, CFFIndex globalSubrs,
                             int[] bias, int[] defaultWidths, int[] nominalWidths, int[] fdSelect) {
        this.isSubset = isSubset;
        this.charStringType = charStringType;
        this.charStrings = charStrings;
        this.fontMatrices = fontMatrices;
        this.isDefaultFontMatrices = getIsDefaultFontMatrices(fontMatrices);
        this.localSubrIndexes = localSubrIndex;
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

    /**
     * Initializes handler with given values.
     *
     * @param isSubset is true if font is subset. In this case all widths will
     *                 be parsed during handler initialization.
     * @param charStringType is type of charstring.
     * @param charStrings is charstring handler with charstring data.
     * @param fontMatrix is font matrix for this charstrings.
     * @param localSubrIndex is CFFIndex with local subrs.
     * @param globalSubrs is CFFIndex with global subrs.
     * @param bias is bias value that depends on local subrs size.
     * @param defaultWidth is a default width for this font program.
     * @param nominalWidth is a nominal width for this font program.
     */
    public CharStringsWidths(boolean isSubset, int charStringType, CFFCharStringsHandler charStrings,
                             float[] fontMatrix, CFFIndex localSubrIndex, CFFIndex globalSubrs,
                             int bias, int defaultWidth, int nominalWidth) {
        this(isSubset, charStringType, charStrings, makeArray(fontMatrix), makeArray(localSubrIndex),
                globalSubrs, makeArray(bias), makeArray(defaultWidth), makeArray(nominalWidth), null);
    }

    /**
     * Gets width for glyph with given gid from charstrings.
     *
     * @param gid is glyph id.
     * @return width for glyph with given gid.
     *
     */
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

    /**
     * @return amount of width in charstrings.
     */
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
                        getLocalSubrs(gid), getLocalBias(gid), globalSubrs, getGlobalBias());
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
        if (!isDefaultFontMatrix(gid)) {
            res *= (getFontMatrix(gid)[0] * 1000);
        }
        return res;
    }

    private int getDefaultWidth(int gid) {
        return getPredefinedValue(gid, this.defaultWidths);
    }

    private int getNominalWidth(int gid) {
        return getPredefinedValue(gid, this.nominalWidths);
    }

    private boolean isDefaultFontMatrix(int gid) {
        if (this.fdSelect == null) {
            return isDefaultFontMatrices[0];
        } else {
            return isDefaultFontMatrices[this.fdSelect[gid]];
        }
    }

    private float[] getFontMatrix(int gid) {
        if (this.fdSelect == null) {
            return fontMatrices[0];
        } else {
            return fontMatrices[this.fdSelect[gid]];
        }
    }

    private int getPredefinedValue(int gid, int[] widthArray) {
        if (this.fdSelect == null) {
            return widthArray[0];
        } else {
            return widthArray[this.fdSelect[gid]];
        }
    }

    private int getLocalBias(int gid) {
        return getPredefinedValue(gid, this.bias);
    }

    private CFFIndex getLocalSubrs(int gid) {
        if (this.fdSelect == null) {
            return localSubrIndexes[0];
        } else {
            return localSubrIndexes[this.fdSelect[gid]];
        }
    }
    
    private int getGlobalBias() {
        int nSubrs = globalSubrs.size();
        if (nSubrs < 1240) {
            return 107;
        } else if (nSubrs < 33900) {
            return 1131;
        } else {
            return 32768;
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

    private static CFFIndex[] makeArray(CFFIndex index) {
        CFFIndex[] res = new CFFIndex[1];
        res[0] = index;
        return res;
    }

    private static float[][] makeArray(float[] matrix) {
        float[][] res = new float[1][];
        res[0] = matrix;
        return res;
    }

    private static boolean[] getIsDefaultFontMatrices(float[][] fontMatrices) {
        boolean[] res = new boolean[fontMatrices.length];
        for (int i = 0; i < fontMatrices.length; ++i) {
            res[i] = Arrays.equals(fontMatrices[i], CFFType1FontProgram.DEFAULT_FONT_MATRIX);
        }
        return res;
    }
}
