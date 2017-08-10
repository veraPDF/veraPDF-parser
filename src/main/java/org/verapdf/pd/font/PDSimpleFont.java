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

import org.verapdf.cos.COSDictionary;
import org.verapdf.pd.font.truetype.AdobeGlyphList;
import org.verapdf.pd.font.type1.SymbolSet;
import org.verapdf.pd.font.type1.ZapfDingbats;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Represents simple font on pd level (Type1, TrueType, Type3).
 *
 * @author Sergey Shemyakov
 */
public abstract class PDSimpleFont extends PDFont {

    private static final Logger LOGGER = Logger.getLogger(PDSimpleFont.class.getCanonicalName());

    /**
     * Constructor from font dictionary.
     * @param dictionary is font dictionary for this simple font.
     */
    public PDSimpleFont(COSDictionary dictionary) {
        super(dictionary);
    }

    /**
     * This method maps character code to a Unicode value. Firstly it checks
     * toUnicode CMap, then it behaves like described in PDF32000_2008 9.10.2
     * "Mapping Character Codes to Unicode Values" for simple font.
     *
     * @param code is code for character.
     * @return Unicode value.
     */
    @Override
    public String toUnicode(int code) {

        String unicodeString = super.toUnicode(code);
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
            AdobeGlyphList.AGLUnicode unicode = AdobeGlyphList.get(glyphName);
            if (unicode != AdobeGlyphList.empty()) {
                return unicode.getUnicodeString();
            }
            LOGGER.log(Level.FINE, "Cannot find glyph " + glyphName + " in Adobe Glyph List.");
            if (ZapfDingbats.hasGlyphName(glyphName) || SymbolSet.hasGlyphName(glyphName)) {
                return " "; // indicates that toUnicode should not be checked.
            }
            return null;
        }
        LOGGER.log(Level.FINE, "Cannot find encoding for glyph with code" + code + " in font " + this.getName());
        return null;
    }
}
