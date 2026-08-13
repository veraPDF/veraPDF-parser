/*
 * This file is part of veraPDF Parser, a module of the veraPDF project.
 * Copyright (c) 2015-2026, veraPDF Consortium <info@verapdf.org>
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
        return toUnicode(code, true);
    }

    @Override
    public String toUnicode(int code, boolean isStrict) {
        String unicodeString = super.toUnicode(code);
        if (unicodeString != null) {
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
            if (isStrict) {
                AdobeGlyphList.AGLUnicode unicode = AdobeGlyphList.get(glyphName);
                if (unicode != AdobeGlyphList.empty()) {
                    return unicode.getUnicodeString();
                }
                LOGGER.log(Level.FINE, "Cannot find glyph " + glyphName + " in Adobe Glyph List.");
                if (ZapfDingbats.hasGlyphName(glyphName) || SymbolSet.hasGlyphName(glyphName)) {
                    return " "; // indicates that toUnicode should not be checked.
                }
            } else {
                if (ZapfDingbats.hasGlyphName(glyphName)) {
                    return ZapfDingbats.toUnicode(glyphName);
                }

                String mapped = mapGlyphNameToUnicode(glyphName);
                if (!mapped.isEmpty()) {
                    return mapped;
                }
                LOGGER.log(Level.FINE, "Cannot find glyph " + glyphName + " in Adobe Glyph List, Zapf Dingbats, or as uni/u name.");
            }

            return null;
        }
        LOGGER.log(Level.FINE, "Cannot find encoding for glyph with code " + code + " in font " + this.getName());
        return null;
    }

    private String mapGlyphNameToUnicode(String glyphName) {
        int dot = glyphName.indexOf('.');
        if (dot >= 0) {
            glyphName = glyphName.substring(0, dot);
        }

        String[] components = glyphName.split("_", -1);
        StringBuilder result = new StringBuilder();
        for (String comp : components) {
            result.append(mapComponent(comp));
        }
        return result.toString();
    }

    private String mapComponent(String component) {
        AdobeGlyphList.AGLUnicode unicode = AdobeGlyphList.get(component);
        if (unicode != AdobeGlyphList.empty()) {
            return unicode.getUnicodeString();
        }

        if (component.startsWith("uni") && component.length() > 3) {
            String hex = component.substring(3);
            if (isValidUniHex(hex)) {
                return decodeUniHex(hex);
            }
        }

        if (component.startsWith("u") && component.length() > 1) {
            String hex = component.substring(1);
            if (isValidUHex(hex)) {
                return decodeUHex(hex);
            }
        }

        return "";
    }

    private boolean isValidUniHex(String hex) {
        if (hex.isEmpty() || hex.length() % 4 != 0) {
            return false;
        }

        if (isNotValidHex(hex)) return false;

        for (int i = 0; i < hex.length(); i += 4) {
            int cp = Integer.parseInt(hex.substring(i, i + 4), 16);
            if (!((cp >= 0x0000 && cp <= 0xD7FF) || (cp >= 0xE000 && cp <= 0xFFFF))) {
                return false;
            }
        }
        return true;
    }

    private String decodeUniHex(String hex) {
        StringBuilder sb = new StringBuilder(hex.length() / 4);
        for (int i = 0; i < hex.length(); i += 4) {
            int cp = Integer.parseInt(hex.substring(i, i + 4), 16);
            sb.append((char) cp);
        }
        return sb.toString();
    }

    private boolean isValidUHex(String hex) {
        if (hex.length() < 4 || hex.length() > 6) {
            return false;
        }

        if (isNotValidHex(hex)) return false;

        int cp = Integer.parseInt(hex, 16);
        return (cp >= 0x0000 && cp <= 0xD7FF) || (cp >= 0xE000 && cp <= 0x10FFFF);
    }

    private String decodeUHex(String hex) {
        int cp = Integer.parseInt(hex, 16);
        if (cp <= 0xFFFF) {
            return String.valueOf((char) cp);
        } else {
            return new String(Character.toChars(cp));
        }
    }

    private boolean isNotValidHex(String hex) {
        for (int i = 0; i < hex.length(); i++) {
            char c = hex.charAt(i);
            if (!((c >= '0' && c <= '9') || (c >= 'A' && c <= 'F'))) {
                return true;
            }
        }
        return false;
    }
}
