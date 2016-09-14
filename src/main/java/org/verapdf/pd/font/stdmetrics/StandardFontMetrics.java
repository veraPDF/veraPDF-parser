package org.verapdf.pd.font.stdmetrics;

import java.util.HashMap;
import java.util.Map;

/**
 * Class provides access to width of 14 standard fonts.
 *
 * @author Sergey Shemyakov
 */
public class StandardFontMetrics {

    private static final String NOTDEF_STRING = ".notdef";

    private Map<String, Integer> widths;

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
}
