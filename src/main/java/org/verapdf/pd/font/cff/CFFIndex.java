package org.verapdf.pd.font.cff;

import java.util.Arrays;

/**
 * This class represents CFF data structure INDEX as described in Adobe
 * Technical Note #5176: "The Compact Font Format Specification".
 *
 * @author Sergey Shemyakov
 */
public class CFFIndex {

    private int count;
    private int offsetShift;
    private int[] offsets;
    private byte[] data;

    CFFIndex(int count, int offsetShift, int[] offsets, byte[] data) {
        this.count = count;
        this.offsetShift = offsetShift;
        this.offsets = offsets;
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
        return Arrays.copyOfRange(data, offsets[n] - 1, offsets[n + 1] - 1);
    }

    int getOffset(int i) {
        return offsets[i];
    }

    int getOffsetShift() {
        return offsetShift;
    }

    int getDataLength() {
        return this.data.length;
    }
}
