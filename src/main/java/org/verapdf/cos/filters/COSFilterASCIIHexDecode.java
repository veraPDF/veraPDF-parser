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

    public final static byte ws = 17;
    public final static byte er = 127;
    COSFilterASCIIReader reader;

    private final static byte[] loHexTable = {
            ws, er, er, er, er, er, er, er, er, ws, ws, er, ws, ws, er, er,    // 0  - 15
            er, er, er, er, er, er, er, er, er, er, er, er, er, er, er, er,    // 16 - 31
            ws, er, er, er, er, er, er, er, er, er, er, er, er, er, er, er,    // 32 - 47
            0, 1, 2, 3, 4, 5, 6, 7, 8, 9, er, er, er, er, er, er,    // 48 - 63
            er, 10, 11, 12, 13, 14, 15, er, er, er, er, er, er, er, er, er,    // 64 - 79
            er, er, er, er, er, er, er, er, er, er, er, er, er, er, er, er,    // 80 - 95
            er, 10, 11, 12, 13, 14, 15, er, er, er, er, er, er, er, er, er,    // 96 - 111
            er, er, er, er, er, er, er, er, er, er, er, er, er, er, er, er,    // 112 - 127
            er, er, er, er, er, er, er, er, er, er, er, er, er, er, er, er,    // 128 - 143
            er, er, er, er, er, er, er, er, er, er, er, er, er, er, er, er,    // 144 - 159
            er, er, er, er, er, er, er, er, er, er, er, er, er, er, er, er,    // 160 - 175
            er, er, er, er, er, er, er, er, er, er, er, er, er, er, er, er,    // 176 - 191
            er, er, er, er, er, er, er, er, er, er, er, er, er, er, er, er,    // 192 - 207
            er, er, er, er, er, er, er, er, er, er, er, er, er, er, er, er,    // 208 - 223
            er, er, er, er, er, er, er, er, er, er, er, er, er, er, er, er,    // 224 - 239
            er, er, er, er, er, er, er, er, er, er, er, er, er, er, er, er    // 240 - 255
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
        byte[] twoBytes = reader.getNextBytes();
        byte res;
        for(int i = 0; i < size - 1; ++i) {
            if(twoBytes == null) {
                return pointer == 0 ? -1 : pointer;
            }
            res = (byte) (decodeLoHex(twoBytes[0]) << 4);
            res += decodeLoHex(twoBytes[1]);
            buffer[pointer++] = res;
            twoBytes = reader.getNextBytes();
        }
        if (pointer == 0) {
            return -1;
        }
        res = (byte) (decodeLoHex(twoBytes[0]) << 4);
        res += decodeLoHex(twoBytes[1]);
        buffer[pointer++] = res;
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
        return loHexTable[val & 0xFF];
    }
}
