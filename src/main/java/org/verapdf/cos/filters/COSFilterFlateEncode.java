/**
 * This file is part of veraPDF Parser, a module of the veraPDF project.
 * Copyright (c) 2015-2024, veraPDF Consortium <info@verapdf.org>
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
import org.verapdf.as.filters.io.ASBufferingOutFilter;
import org.verapdf.as.io.ASInputStream;
import org.verapdf.as.io.ASOutputStream;

import java.io.IOException;
import java.util.zip.Deflater;
import java.util.zip.DeflaterInputStream;

/**
 * Filter that implements flate encoding.
 *
 * @author Sergey Shemyakov
 */
public class COSFilterFlateEncode extends ASBufferingOutFilter {

    public COSFilterFlateEncode(ASOutputStream stream) {
        super(stream);
    }

    /**
     * Flate encodes given data buffer.
     *
     * @param buffer is buffer to be encoded.
     * @return length of encoded data buffer.
     * @throws IOException
     */
    @Override
    public long write(byte[] buffer) throws IOException {
        Deflater deflater = new Deflater();
        deflater.setInput(buffer);
        deflater.finish();
        int res = 0;
        int deflated = -1;
        while (deflated != 0) {
            deflated = deflater.deflate(this.internalBuffer, 0,
                    this.internalBuffer.length);
            this.getStoredOutputStream().write(this.internalBuffer, 0, deflated);
            res += deflated;
        }
        deflater.finish();
        return res;
    }

    /**
     * Flate encodes given data buffer.
     *
     * @param buffer is buffer to be encoded.
     * @param offset is offset of data beginning in buffer.
     * @param size   is length of data in buffer in bytes.
     * @return length of encoded data buffer.
     * @throws IOException
     */
    @Override
    public long write(byte[] buffer, int offset, int size) throws IOException {
        Deflater deflater = new Deflater();
        deflater.setInput(buffer, offset, size);
        deflater.finish();
        int res = 0;
        int deflated = -1;
        while (deflated != 0) {
            deflated = deflater.deflate(this.internalBuffer, 0,
                    this.internalBuffer.length);
            this.getStoredOutputStream().write(this.internalBuffer, 0, deflated);
            res += deflated;
        }
        deflater.finish();
        return res;
    }

    @Override
    public long write(ASInputStream stream) throws IOException {
        DeflaterInputStream deflaterInputStream = new DeflaterInputStream(stream);
        int res = 0;
        byte[] buf = new byte[ASBufferedInFilter.BF_BUFFER_SIZE];
        int deflated = deflaterInputStream.read(buf);
        while (deflated > 0) {
            this.getStoredOutputStream().write(buf, 0, deflated);
            res += deflated;
            deflated = deflaterInputStream.read(buf);
        }
        return res;
    }
}
