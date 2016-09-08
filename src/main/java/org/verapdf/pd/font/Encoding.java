package org.verapdf.pd.font;

import org.verapdf.as.ASAtom;
import org.verapdf.pd.font.truetype.TrueTypePredefined;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents encoding of font as given in font dictionary.
 *
 * @author Sergey Shemyakov
 */
public class Encoding {

    private String[] predefinedEncoding;
    private Map<Integer, String> differences;

    /**
     * Constructor for encoding of type COSName.
     *
     * @param predefinedEncoding is ASAtom value of Encoding.
     */
    public Encoding(ASAtom predefinedEncoding) {
        if (predefinedEncoding == ASAtom.MAC_ROMAN_ENCODING) {
            this.predefinedEncoding = TrueTypePredefined.MAC_ROMAN_ENCODING;
        } else if (predefinedEncoding == ASAtom.MAC_EXPERT_ENCODING) {
            this.predefinedEncoding = TrueTypePredefined.MAC_EXPERT_ENCODING;
        } else if (predefinedEncoding == ASAtom.WIN_ANSI_ENCODING) {
            this.predefinedEncoding = TrueTypePredefined.WIN_ANSI_ENCODING;
        }
    }

    /**
     * Constructor for encoding of type COSDictionary.
     *
     * @param baseEncoding is ASAtom representation of BaseEncoding entry in
     *                     Encoding.
     * @param differences  is Map representation of Differences entry in
     *                     Encoding.
     */
    public Encoding(ASAtom baseEncoding, Map<Integer, String> differences) {
        this(baseEncoding);
        if (differences != null) {
            this.differences = differences;
        } else {
            this.differences = new HashMap<>();
        }
    }

    /**
     * Gets name of char for it's code via this encoding.
     *
     * @param code is character code.
     * @return glyph name for given character code.
     */
    public String getName(int code) {
        if (differences == null) {
            if (code < predefinedEncoding.length) {
                return predefinedEncoding[code];
            } else {
                return predefinedEncoding[0];   //  .notdef
            }
        } else {
            String diffRes = this.differences.get(code);
            if (diffRes == null) {
                if (code < predefinedEncoding.length) {
                    diffRes = predefinedEncoding[code];
                } else {
                    diffRes = predefinedEncoding[0];   //  .notdef
                }
            }
            return diffRes;
        }
    }

}
