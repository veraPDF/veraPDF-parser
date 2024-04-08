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
package org.verapdf.cos.filters;

import org.verapdf.as.filters.io.ASBufferedInFilter;
import org.verapdf.as.filters.io.COSFilterASCIIReader;
import org.verapdf.as.io.ASInputStream;

import java.io.IOException;

/**
 * Filter for ASCIIHex data decoding.
 *
 * @author Timur Kamalov
 */
public class COSFilterASCIIHexDecode extends ASBufferedInFilter {

    private static final byte WS = 17;
    public static final byte ER = 127;
    private final COSFilterASCIIReader reader;

    private static final byte[] LO_HEX_TABLE = {
            WS, ER, ER, ER, ER, ER, ER, ER, ER, WS, WS, ER, WS, WS, ER, ER,    // 0  - 15
            ER, ER, ER, ER, ER, ER, ER, ER, ER, ER, ER, ER, ER, ER, ER, ER,    // 16 - 31
            WS, ER, ER, ER, ER, ER, ER, ER, ER, ER, ER, ER, ER, ER, ER, ER,    // 32 - 47
            0, 1, 2, 3, 4, 5, 6, 7, 8, 9, ER, ER, ER, ER, ER, ER,    // 48 - 63
            ER, 10, 11, 12, 13, 14, 15, ER, ER, ER, ER, ER, ER, ER, ER, ER,    // 64 - 79
            ER, ER, ER, ER, ER, ER, ER, ER, ER, ER, ER, ER, ER, ER, ER, ER,    // 80 - 95
            ER, 10, 11, 12, 13, 14, 15, ER, ER, ER, ER, ER, ER, ER, ER, ER,    // 96 - 111
            ER, ER, ER, ER, ER, ER, ER, ER, ER, ER, ER, ER, ER, ER, ER, ER,    // 112 - 127
            ER, ER, ER, ER, ER, ER, ER, ER, ER, ER, ER, ER, ER, ER, ER, ER,    // 128 - 143
            ER, ER, ER, ER, ER, ER, ER, ER, ER, ER, ER, ER, ER, ER, ER, ER,    // 144 - 159
            ER, ER, ER, ER, ER, ER, ER, ER, ER, ER, ER, ER, ER, ER, ER, ER,    // 160 - 175
            ER, ER, ER, ER, ER, ER, ER, ER, ER, ER, ER, ER, ER, ER, ER, ER,    // 176 - 191
            ER, ER, ER, ER, ER, ER, ER, ER, ER, ER, ER, ER, ER, ER, ER, ER,    // 192 - 207
            ER, ER, ER, ER, ER, ER, ER, ER, ER, ER, ER, ER, ER, ER, ER, ER,    // 208 - 223
            ER, ER, ER, ER, ER, ER, ER, ER, ER, ER, ER, ER, ER, ER, ER, ER,    // 224 - 239
            ER, ER, ER, ER, ER, ER, ER, ER, ER, ER, ER, ER, ER, ER, ER, ER    // 240 - 255
    };

    /**
     * Constructor from encoded stream.
     * @param stream is ASCII Hex encoded stream.
     * @throws IOException
     */
    public COSFilterASCIIHexDecode(ASInputStream stream) throws IOException {
        super(stream);
        reader = new COSFilterASCIIReader(stream, true);
    }

    /**
     * Reads up to size bytes of ASCII Hex decoded data into buffer.
     *
     * @param buffer is byte array where decoded data will be read.
     * @param size   is maximal amount of decoded bytes.
     * @return amount of actually read bytes.
     * @throws IOException
     */
    @Override
    public int read(byte[] buffer, int size) throws IOException {
        int pointer = 0;
        for (int i = 0; i < size; ++i) {
            byte[] twoBytes = reader.getNextBytes();
            if (twoBytes == null) {
                return pointer == 0 ? -1 : pointer;
            }
            byte res = (byte) (decodeLoHex(twoBytes[0]) << 4);
            res += decodeLoHex(twoBytes[1]);
            buffer[pointer++] = res;
        }
        return pointer == 0 ? -1 : pointer;
    }

    /**
     * Converts char byte to it's hex value, e.g. '1' -> 1 and 'A' -> 10.
     *
     * @param val is character that should be hexadecimal digit.
     * @return actual value for this character or 127 if character is not a
     * valid hex digit.
     */
    public static byte decodeLoHex(byte val) {
        return LO_HEX_TABLE[val & 0xFF];
    }
}
