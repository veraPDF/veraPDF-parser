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
package org.verapdf.pd.font.cmap;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.logging.Logger;

/**
 * Represents interval of mappings to Unicode.
 *
 * @author Sergey Shemyakov
 */
public class ToUnicodeInterval {

    private static final Logger LOGGER = Logger.getLogger(ToUnicodeInterval.class.getCanonicalName());

    private long intervalBegin;
    private long intervalEnd;
    private byte[] startingValue;

    /**
     * @param intervalBegin is the first code of mapping interval.
     * @param intervalEnd is the last code of mapping interval.
     * @param startingValue is the cid value for first code of mapping interval.
     */
    public ToUnicodeInterval(long intervalBegin, long intervalEnd, byte[] startingValue) {
        this.intervalBegin = intervalBegin;
        this.intervalEnd = intervalEnd;
        this.startingValue = startingValue;
    }

    /**
     * Checks if given code can be successfully mapped to cid with this mapping
     * interval.
     *
     * @param code is character code to be checked.
     * @return mapping for code is present in this mapping interval.
     */
    public boolean containsCode(long code) {
        return code >= intervalBegin && code <= intervalEnd;
    }

    /**
     * If code is in mapping interval, this method performs mapping of given
     * code to Unicode value. If code is not in interval, return value is
     * undefined.
     *
     * @param code is a character code.
     * @return Unicode value for character code as a String object.
     */
    public String toUnicode(int code) {
        byte[] unicode = Arrays.copyOf(startingValue, startingValue.length);
        unicode[unicode.length - 1] = (byte) (code - intervalBegin + startingValue[startingValue.length - 1]);
        return getUnicodeNameFromLong(unicode);
    }

    private static String getUnicodeNameFromLong(byte[] unicode) {
        String fffe = getFFFEFromUnicode(unicode);
        if (fffe == null) {
            fffe = getFEFFFromUnicode(unicode);
        }
        if (fffe != null) {
            return fffe;
        }
        return  (unicode[0] == 0) ? String.valueOf((char)unicode[1]) : new String(unicode, StandardCharsets.UTF_16BE);
    }

    private static String getFFFEFromUnicode(byte[] unicode) {
        for (int i = 0; i < unicode.length - 1; ++i) {
            if (unicode[i] == (byte) 0xFF && unicode[i+1] == (byte) 0xFE) {
                char[] c = new char[] {0xFFFE};
                return new String(c);
            }
        }
        return null;
    }

    private static String getFEFFFromUnicode(byte[] unicode) {
        for (int i = 0; i < unicode.length - 1; ++i) {
            if (unicode[i] == (byte) 0xFE && unicode[i+1] == (byte) 0xFF) {
                char[] c = new char[] {0xFFFE};
                return new String(c);
            }
        }
        return null;
    }
}
