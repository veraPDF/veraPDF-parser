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
package org.verapdf.as.filters.io;

import org.verapdf.as.filters.ASOutFilter;
import org.verapdf.as.io.ASOutputStream;

/**
 * @author Sergey Shemyakov
 */
public class ASBufferingOutFilter extends ASOutFilter {

    private final int bufferCapacity;
    protected byte [] internalBuffer;
    private int bufferWriter;
    private final int bufferEnd;

    public ASBufferingOutFilter(ASOutputStream stream) {
        this(stream, ASBufferedInFilter.BF_BUFFER_SIZE);
    }

    public ASBufferingOutFilter(ASOutputStream stream, int bufferCapacity) {
        super(stream);
        this.bufferCapacity = bufferCapacity;
        internalBuffer = new byte[bufferCapacity];
        bufferWriter = 0;
        bufferEnd = bufferCapacity;
    }

    @Override
    public void close() {
        this.internalBuffer = null;
        super.close();
    }

    /**
     * @return the index of current write position.
     */
    public int getBufferWriter() {
        return bufferWriter;
    }

    /**
     * @return the index of the end of the buffer.
     */
    public int getBufferEnd() {
        return bufferEnd;
    }

    /**
     * @return the total capacity of buffer.
     */
    public int getBufferCapacity() {
        return bufferCapacity;
    }

    /**
     * @return number of bytes actually present in the buffer.
     */
    public int bufferSize() {
        return bufferWriter;
    }

    /**
     * Stores character to current writer position and increments writer position.
     * For better performance does not check buffer overflow, use with care.
     * @param b is character to be put into buffer.
     */
    public void storeChar(byte b) {
        internalBuffer[bufferWriter++] = b;
    }

    /**
     * Moves buffer writer pointer back by given number of bytes.
     * @param offset is number of bytes on which we should move pointer.
     * @return actual amount of bytes on which pointer was moved.
     */
    public int bufferRewind(int offset) {
        int actualOffset = Math.min(bufferWriter, offset);
        bufferWriter -= actualOffset;
        return actualOffset;
    }
}
