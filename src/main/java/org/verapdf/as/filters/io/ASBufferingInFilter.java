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
package org.verapdf.as.filters.io;

import org.verapdf.as.filters.ASInFilter;
import org.verapdf.as.io.ASInputStream;

import java.io.IOException;
import java.util.Arrays;

/**
 * Class provides buffered input from input stream.
 *
 * @author Sergey Shemyakov
 */
public class ASBufferingInFilter extends ASInFilter {

    public static final int BF_BUFFER_SIZE = 2048;

    private int bufferCapacity;
    protected byte[] internalBuffer;
    private int bufferBegin, bufferEnd;

    public ASBufferingInFilter(ASInputStream stream) throws IOException {
        this(stream, BF_BUFFER_SIZE);
    }

    public ASBufferingInFilter(ASInputStream stream, int buffCapacity) throws IOException {
        super(stream);
        this.bufferCapacity = buffCapacity;
        internalBuffer = new byte[buffCapacity];
        bufferEnd = bufferBegin = 0;
    }

    public ASBufferingInFilter(ASBufferingInFilter filter) {
        super(filter);
        this.bufferCapacity = filter.bufferCapacity;
        internalBuffer = new byte[bufferCapacity];
        this.bufferBegin = filter.bufferBegin;
        this.bufferEnd = filter.bufferEnd;
        if (this.bufferSize() > 0) {
            internalBuffer = Arrays.copyOfRange(filter.internalBuffer,
                    filter.bufferBegin, filter.bufferEnd);
        }
    }

    /**
     * Shifts begin marker by up to bytesToProceed bytes to the right of to the
     * end of the buffer if bytesToProceed is too big.
     *
     * @param bytesToProcess amount of bytes to shift.
     * @return amount of bytes actually processed.
     */
    public int processBuffer(int bytesToProcess) {
        int actuallyProcessed = Math.min(bytesToProcess, bufferSize());
        bufferBegin += bytesToProcess;
        return actuallyProcessed;
    }

    /**
     * Reads next portion of data from the underlying stream to the internal
     * buffer, updates begin and end pointers and returns number of bytes
     * actually placed in buffer.
     *
     * @param bytesToRead amount of bytes to read.
     * @return amount of bytes actually placed into buffer.
     */
    public long feedBuffer(int bytesToRead) throws IOException {
        if (this.getInputStream() == null) {
            return -1;
        }
        bytesToRead = Math.min(bytesToRead, bufferCapacity);
        long actuallyRead = this.getInputStream().read(internalBuffer, bytesToRead);
        bufferBegin = 0;
        bufferEnd = (int) actuallyRead;
        return actuallyRead;
    }

    /**
     * Reads next portion of data from the underlying stream and appends it to
     * the end of data, contained in internal buffer.
     *
     * @param bytesToAdd amount of bytes to read.
     * @return amount of bytes actually appended to buffer.
     */
    public long addToBuffer(int bytesToAdd) throws IOException {
        if (this.getInputStream() == null) {
            return -1;
        }
        bytesToAdd = Math.min(bytesToAdd, bufferCapacity - bufferEnd);
        byte[] toAdd = new byte[bytesToAdd];
        long actuallyRead = this.getInputStream().read(toAdd, bytesToAdd);
        System.arraycopy(toAdd, 0, this.internalBuffer, bufferEnd, bytesToAdd);
        bufferEnd += bytesToAdd;
        return actuallyRead;
    }

    /**
     * @return beginning index of unread data in buffer.
     */
    public int getBufferBegin() {
        return bufferBegin;
    }

    /**
     * @return index of the end of of valid unread data in buffer.
     */
    public int getBufferEnd() {
        return bufferEnd;
    }

    /**
     * Returns the character pointed by buffer begin marker and advances it.
     *
     * @return character, pointed by buffer begin marker.
     */
    public byte bufferPop() {
        return internalBuffer[bufferBegin++];
    }

    /**
     * Reads data from internal buffer into passed byte array and advances begin
     * marker.
     *
     * @param buffer is byte array where data will be read.
     * @param read   maximal amount of bytes to read.
     * @return amount of actually read bytes.
     * @throws IOException if passed buffer is too small to contain necessary
     *                     amount of bytes.
     */
    public int bufferPopArray(byte[] buffer, int read) throws IOException {
        int actualRead = Math.min(read, bufferSize());
        if(actualRead == -1) {
            return -1;
        }
        if (buffer.length < actualRead) {
            throw new IOException("Passed buffer is too small");
        }
        System.arraycopy(this.internalBuffer, bufferBegin, buffer, 0, actualRead);
        bufferBegin += actualRead;
        return actualRead;
    }

    /**
     * @return the number of bytes currently available in the buffer.
     */
    public int bufferSize() {
        return bufferEnd - bufferBegin;
    }

    /**
     * @return the total capacity of buffer.
     */
    public int getBufferCapacity() {
        return bufferCapacity;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void closeResource() throws IOException {
        super.closeResource();
        bufferEnd = bufferBegin = 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void reset() throws IOException {
        super.reset();
        bufferEnd = bufferBegin = 0;
    }

    public static byte[] concatenate(byte[] one, int lengthOne, byte[] two, int lengthTwo) {
        if (lengthTwo == -1) {
            lengthTwo = 0;
        }
        if (lengthOne == -1) {
            lengthOne = 0;
        }
        if (lengthOne == 0) {
            return Arrays.copyOfRange(two, 0, lengthTwo);
        }
        if (lengthTwo == 0) {
            return Arrays.copyOfRange(one, 0, lengthOne);
        }
        byte[] res = new byte[lengthOne + lengthTwo];
        System.arraycopy(one, 0, res, 0, lengthOne);
        System.arraycopy(two, 0, res, lengthOne, lengthTwo);
        return res;
    }

    protected void decode() throws IOException {
    }

    /**
     * Skips given number of decoded bytes in stream.
     *
     * @param size is amount of bytes to skip.
     * @return amount of actually skipped bytes.
     * @throws IOException if stream-reading error occurs.
     */
    @Override
    public int skip(int size) throws IOException {
        byte[] temp = new byte[Math.min(BF_BUFFER_SIZE, size)];
        int skipped = 0;
        while (skipped != size) {
            int read = this.read(temp, Math.min(size - skipped, BF_BUFFER_SIZE));
            if (read == -1) {
                return skipped;
            } else {
                skipped += read;
            }
        }
        return skipped;
    }
}
