package org.verapdf.pd.font;

import org.apache.log4j.Logger;
import org.verapdf.cos.COSDictionary;
import org.verapdf.pd.font.truetype.AdobeGlyphList;

/**
 * Represents simple font on pd level (Type1, TrueType, Type3).
 *
 * @author Sergey Shemyakov
 */
public abstract class PDSimpleFont extends PDFont {

    private static final Logger LOGGER = Logger.getLogger(PDSimpleFont.class);

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
        if (fontEncoding != null) {
            String glyphName = fontEncoding.getName(code);
            if (glyphName != null) {
                AdobeGlyphList.AGLUnicode unicode = AdobeGlyphList.get(glyphName);
                if (unicode != AdobeGlyphList.empty()) {
                    return unicode.getUnicodeString();
                }
                LOGGER.warn("Cannot find glyph " + glyphName + " in Adobe Glyph List.");
                return null;
            }
        }
        LOGGER.warn("Cannot find encoding for glyph with code" + code + " in font " + this.getName());
        return null;
    }
}
