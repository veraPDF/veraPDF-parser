package org.verapdf.font.cff;

import java.util.Arrays;

/**
 * This class represents CFF data structure INDEX as described in Adobe
 * Technical Note #5176: "The Compact Font Format Specification".
 *
 * @author Sergey Shemyakov
 */
class Index {

    private int count;
    private int[] offset;
    private byte[] data;

    Index(int count, int[] offset, byte[] data) {
        this.count = count;
        this.offset = offset;
        this.data = data;
    }

    int size() {
        return count;
    }

    byte[] get(int n) {
        if (n >= count) {
            throw new ArrayIndexOutOfBoundsException("Can't get object with number "
                    + n + " from INDEX with " + count + " elements.");
        }
        return Arrays.copyOfRange(data, offset[n], offset[n + 1]);
    }

}
