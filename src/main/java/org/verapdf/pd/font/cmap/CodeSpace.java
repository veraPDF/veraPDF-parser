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

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class represents codespace range.
 *
 * @author Sergey Shemyakov
 */
class CodeSpace {
    private final byte[] begin;
    private final byte[] end;

    private static final Logger LOGGER = Logger.getLogger(CodeSpace.class.getCanonicalName());

    /**
     * Constructor for codespace range.
     *
     * @param begin is array of bytes, representing codespace range beginning.
     * @param end   is array of bytes, representing codespace range end.
     */
    CodeSpace(byte[] begin, byte[] end) {
        if (begin.length == end.length) {
            for (int i = 0; i < begin.length; ++i) {
                int beginNum = begin[i] & 0xFF;
                int endNum = end[i] & 0xFF;
                if (beginNum <= endNum) {
                    continue;
                }
                this.begin = new byte[0];
                this.end = new byte[0];
                LOGGER.log(Level.FINE, "In codespace byte " + i + " in begin array is bigger than in end array.");
                return;
            }
            this.begin = begin;
            this.end = end;
        } else {
            this.begin = new byte[0];
            this.end = new byte[0];
            LOGGER.log(Level.FINE, "In codespace two passed arrays have different lengths");
        }
    }

    /**
     * Returns true if given character lies inside this codespace range.
     *
     * @param character is character to check.
     * @return true if given character lies inside this codespace range.
     */
    public boolean contains(byte[] character) {
        if (begin.length == character.length) {
            for (int i = 0; i < character.length; ++i) {
                int beginNum = begin[i] & 0xFF;
                int endNum = end[i] & 0xFF;
                int charNum = character[i] & 0xFF;
                if (charNum >= beginNum && charNum <= endNum) {
                    continue;
                }
                return false;
            }
            return true;
        }
        return false;
    }

    /**
     * Checks partial match in given codespace range.
     *
     * @param toBeMatched is byte we are checking.
     * @param position    is position at which we are looking for a match.
     * @return true if there is a match.
     */
    public boolean isPartialMatch(byte toBeMatched, int position) {
        int beginNum = begin[position] & 0xFF;
        int endNum = end[position] & 0xFF;
        int charNum = toBeMatched & 0xFF;
        return charNum >= beginNum && charNum <= endNum;
    }

    /**
     * Checks if two codespace ranges overlap.
     *
     * @param another is a cosespace with which we are checking overlapping.
     * @return true if codespaces overlap.
     */
    public boolean overlaps(CodeSpace another) {
        int minLen = Math.min(this.getLength(), another.getLength());
        for (int i = 0; i < minLen; ++i) {
            int begin1 = this.begin[i] & 0xFF;
            int begin2 = another.begin[i] & 0xFF;
            int end1 = this.end[i] & 0xFF;
            int end2 = another.end[i] & 0xFF;
            if ((begin2 > end1 && end2 > end1) || (begin2 < begin1 && end2 < begin1)) {
                return false;
            }
        }
        return true;
    }

    int getLength() {
        return this.begin.length;
    }
}
