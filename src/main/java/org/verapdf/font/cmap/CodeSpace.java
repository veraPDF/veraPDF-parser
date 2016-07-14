package org.verapdf.font.cmap;

import org.apache.log4j.Logger;

/**
 * Class represents codespace range.
 *
 * @author Sergey Shemyakov
 */
class CodeSpace {
    private byte[] begin, end;

    private static final Logger LOGGER = Logger.getLogger(CodeSpace.class);

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
                LOGGER.debug("In codespace byte " + i + "in begin array is bigger than in end array.");
                return;
            }
            this.begin = begin;
            this.end = end;
        } else {
            this.begin = new byte[0];
            this.end = new byte[0];
            LOGGER.debug("In codespace two passed arrays have different lengths");
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
