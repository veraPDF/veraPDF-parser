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
package org.verapdf.pd.font.stdmetrics;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Class provides access to width of 14 standard fonts.
 *
 * @author Sergey Shemyakov
 */
public class StandardFontMetrics {

    private static final String NOTDEF_STRING = ".notdef";

    private Map<String, Integer> widths;

    // values
    private String fontName;
    private String familyName;
    private double[] fontBBox;
    private String encodingScheme;
    private String charSet;
    private Double capHeight;
    private Double XHeight;
    private Double ascend;
    private Double descend;
    private Double italicAngle;

    public StandardFontMetrics() {
        this.widths = new HashMap<>();
    }

    void putWidth(String glyphName, int width) {
        this.widths.put(glyphName, width);
    }

    /**
     * Gets width for glyph with given name in this font. If there is no
     * glyph with given name, gets width of .notdef glyph.
     *
     * @param glyphName is name of glyph.
     * @return width of this glyph or width of .notdef glyph.
     */
    public int getWidth(String glyphName) {
        Integer res = this.widths.get(glyphName);
        return res == null ? this.widths.get(NOTDEF_STRING) : res;
    }

    public String getFontName() {
        return fontName;
    }

    public void setFontName(String fontName) {
        this.fontName = fontName;
    }

    public String getFamilyName() {
        return familyName;
    }

    public void setFamilyName(String familyName) {
        this.familyName = familyName;
    }

    public double[] getFontBBox() {
        return fontBBox;
    }

    public void setFontBBox(double[] fontBBox) {
        this.fontBBox = fontBBox;
    }

    public String getEncodingScheme() {
        return encodingScheme;
    }

    public void setEncodingScheme(String encodingScheme) {
        this.encodingScheme = encodingScheme;
    }

    public String getCharSet() {
        return charSet;
    }

    public void setCharSet(String charSet) {
        this.charSet = charSet;
    }

    public Double getCapHeight() {
        return capHeight;
    }

    public void setCapHeight(Double capHeight) {
        this.capHeight = capHeight;
    }

    public Double getXHeight() {
        return XHeight;
    }

    public void setXHeight(Double XHeight) {
        this.XHeight = XHeight;
    }

    public Double getAscend() {
        return ascend;
    }

    public void setAscend(Double ascend) {
        this.ascend = ascend;
    }

    public Double getDescend() {
        return descend;
    }

    public void setDescend(Double descend) {
        this.descend = descend;
    }

    public Double getItalicAngle() {
        return italicAngle;
    }

    public void setItalicAngle(Double italicAngle) {
        this.italicAngle = italicAngle;
    }

    public Iterator<Map.Entry<String, Integer>> getWidthsIterator() {
        return this.widths.entrySet().iterator();
    }
}
