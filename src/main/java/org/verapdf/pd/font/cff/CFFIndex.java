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
