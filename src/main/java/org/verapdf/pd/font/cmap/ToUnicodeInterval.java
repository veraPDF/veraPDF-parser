/**
 * This file is part of veraPDF Parser, a module of the veraPDF project.
 * Copyright (c) 2015, veraPDF Consortium <info@verapdf.org>
 * All rights reserved.
 * <p>
 * veraPDF Parser is free software: you can redistribute it and/or modify
 * it under the terms of either:
 * <p>
 * The GNU General public license GPLv3+.
 * You should have received a copy of the GNU General Public License
 * along with veraPDF Parser as the LICENSE.GPL file in the root of the source
 * tree.  If not, see http://www.gnu.org/licenses/ or
 * https://www.gnu.org/licenses/gpl-3.0.en.html.
 * <p>
 * The Mozilla Public License MPLv2+.
 * You should have received a copy of the Mozilla Public License along with
 * veraPDF Parser as the LICENSE.MPL file in the root of the source tree.
 * If a copy of the MPL was not distributed with this file, you can obtain one at
 * http://mozilla.org/MPL/2.0/.
 */
package org.verapdf.pd.font.cmap;

import java.io.UnsupportedEncodingException;
import java.util.logging.Level;
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
    private long startingValue;

    /**
     * @param intervalBegin is the first code of mapping interval.
     * @param intervalEnd is the last code of mapping interval.
     * @param startingValue is the cid value for first code of mapping interval.
     */
    public ToUnicodeInterval(long intervalBegin, long intervalEnd, long startingValue) {
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
        long unicode = code - intervalBegin + startingValue;
        return getUnicodeNameFromLong(unicode);
    }

    private static String getUnicodeNameFromLong(long unicode) {
        byte[] arr = new byte[2];
        arr[1] = (byte) (unicode & 0xFF);
        unicode >>= 8;
        arr[0] = (byte) (unicode & 0xFF);
        try {
            if (arr[0] == 0) {
                return String.valueOf(arr[1]);
            }
            return new String(arr, "UTF-16BE");
        } catch (UnsupportedEncodingException e) {
            LOGGER.log(Level.FINE, "Can't find String encoding UTF-16BE", e);
            return null;    // I'm sure this won't be reached
        }
    }
}
