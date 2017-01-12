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
