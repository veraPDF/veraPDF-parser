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
 * Filter for ASCII 85 data decoding.
 *
 * @author Sergey Shemyakov
 */
public class COSFilterASCII85Decode extends ASBufferedInFilter {

    private COSFilterASCIIReader reader;

    /**
     * Constructor from encoded stream.
     *
     * @param stream is ASCII85 encoded stream.
     * @throws IOException
     */
    public COSFilterASCII85Decode(ASInputStream stream) throws IOException {
        super(stream);
        reader = new COSFilterASCIIReader(stream, false);
    }

    /**
     * Reads up to size bytes of ASCII85 decoded data into buffer.
     *
     * @param buffer is byte array where decoded data will be read.
     * @param size   is maximal amount of decoded bytes. It should be at least 4.
     * @return amount of actually read bytes.
     * @throws IOException
     */
    @Override
    public int read(byte[] buffer, int size) throws IOException {
        int pointer = 0;
        byte[] fourBytes = new byte[4];
        while (pointer + 4 <= size) {
            byte[] fiveBytes = reader.getNextBytes();
            if(fiveBytes == null) {
                break;
            }
            int decoded = decodeFiveBytes(fiveBytes, fourBytes);
            System.arraycopy(fourBytes, 0, buffer, pointer, decoded);
            pointer += decoded;
        }
        return pointer == 0 ? -1 : pointer;
    }

    private int decodeFiveBytes(byte[] fiveBytes, byte[] fourBytes) {
        long value = 0;
        for(int i = 0; i < fiveBytes.length; ++i) {
            value += fiveBytes[i] - '!';
            value *= 85;
        }
        for(int i = 0; i < 5 - fiveBytes.length; ++i) { // This processes situation of last portion of bytes that can have length != 5.
            value += 'u' - '!';
            value *= 85;
        }
        value /= 85;
        for(int i = 3; i >= 0; i--) {
            fourBytes[i] = (byte) (value % 256);
            value >>= 8;
        }
        return fiveBytes.length - 1;
    }
}
