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

import org.verapdf.io.SeekableInputStream;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class handles cff charstrings.
 *
 * @author Sergey Shemyakov
 */
class CFFCharStringsHandler {

    private static final Logger LOGGER = Logger.getLogger(
            CFFCharStringsHandler.class.getCanonicalName());

    private static final int MAX_BUFFER_SIZE = 10240;

    private int amount;
    private CFFIndex memoryInCharStirngs;
    private SeekableInputStream fontStream;
    private long[] charStringsOffsets;

    CFFCharStringsHandler(CFFIndex charStrings, long charStringsOffset,
                          SeekableInputStream fontStream) {
        this.amount = charStrings.size();
        if (charStrings.getDataLength() < MAX_BUFFER_SIZE) {
            this.memoryInCharStirngs = charStrings;
        } else {
            this.fontStream = fontStream;
            this.charStringsOffsets = new long[charStrings.size() + 1];
            for(int i = 0; i < charStrings.size() + 1; ++i) {
                this.charStringsOffsets[i] = charStringsOffset +
                        charStrings.getOffsetShift() + charStrings.getOffset(i) - 1;
            }
        }
    }

    byte[] getCharString(int num) throws IOException {
        if(num >= 0 && num < this.amount) {
            if (memoryInCharStirngs != null) {
                return memoryInCharStirngs.get(num);
            } else {
                long offset = this.fontStream.getOffset();
                this.fontStream.seek(charStringsOffsets[num]);
                byte[] res = new byte[(int) (charStringsOffsets[num + 1] -
                        charStringsOffsets[num])];
                fontStream.read(res, res.length);
                fontStream.seek(offset);
                return res;
            }
        } else {
            LOGGER.log(Level.FINE, "Cannot obtain charstring " + num + ", " +
                    "total " + amount + "charstrings ");
            return new byte[]{};
        }
    }

    int getCharStringAmount() {
        return this.amount;
    }
}
