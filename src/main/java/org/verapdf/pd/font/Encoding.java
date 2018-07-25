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

import org.verapdf.as.ASAtom;
import org.verapdf.pd.font.truetype.TrueTypePredefined;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Represents encoding of font as given in font dictionary.
 *
 * @author Sergey Shemyakov
 */
public class Encoding {

    private static final Logger LOGGER = Logger.getLogger(Encoding.class.getCanonicalName());

    private static final Encoding EMPTY = new Encoding(null);

    private static final String NOTDEF = ".notdef";

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
        } else {
            this.predefinedEncoding = new String[0];
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

    public static Encoding empty() {
        return EMPTY;
    }

    /**
     * Gets name of char for it's code via this encoding.
     *
     * @param code is character code.
     * @return glyph name for given character code or null if the internal font encoding should be used.
     */
    public String getName(int code) {
        if (code >= 0) {
            if (differences == null) {
                if (code < predefinedEncoding.length) {
                    return predefinedEncoding[code];
                } else {
                    // if no predefined encoding, the null result for using font encoding
                    return (predefinedEncoding.length != 0) ? NOTDEF : null;
                }
            } else {
                String diffRes = this.differences.get(code);
                if (diffRes == null && predefinedEncoding.length != 0) {
                    diffRes = (code < predefinedEncoding.length) ? predefinedEncoding[code] : NOTDEF;
                }
                return diffRes;
            }
        } else {
            LOGGER.log(Level.WARNING, "Invalid glyph code: " + code);
            return null;
        }
    }

    /**
     * Checks if mapping for given code is available.
     *
     * @param code is character code to check.
     * @return true if encoding has mapping for this code.
     */
    public boolean containsCode(int code) {
        if (differences != null && differences.containsKey(code)) {
            return true;
        }
        return code < predefinedEncoding.length;
    }
}
