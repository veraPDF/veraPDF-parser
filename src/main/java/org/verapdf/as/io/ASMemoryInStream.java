/**
 * This file is part of veraPDF Parser, a module of the veraPDF project.
 * Copyright (c) 2015-2025, veraPDF Consortium <info@verapdf.org>
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
package org.verapdf.as.io;

import org.verapdf.io.SeekableInputStream;
import org.verapdf.tools.IntReference;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

/**
 * This class binds the SeekableInputStream interface to a memory buffer.
 *
 * @author Sergey Shemyakov
 */
public class ASMemoryInStream extends SeekableInputStream {

    private final int bufferSize;
    private int bufferOffset = 0;
    private int currentPosition;
    private byte[] buffer;
    private final boolean copiedBuffer;
    private int resetPosition = 0;
    private final IntReference numOfBufferUsers;

    /**
     * Constructor from byte array. Buffer is copied while initializing
     * ASMemoryInStream.
     *
     * @param buffer byte array containing data.
     */
    public ASMemoryInStream(byte[] buffer) {
        this(buffer, buffer.length);
    }

    /**
     * Constructor from other stream. It reads stream into byte buffer.
     *
     * @param stream is stream to read into byte array.
     */
    public ASMemoryInStream(InputStream stream) throws IOException {
        this.currentPosition = 0;
        this.copiedBuffer = true;
        byte[] buffer = new byte[8192];
        int bytesRead;
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        while ((bytesRead = stream.read(buffer)) != -1) {
            output.write(buffer, 0, bytesRead);
        }
        this.buffer = output.toByteArray();
        bufferSize = this.buffer.length;
        this.numOfBufferUsers = new IntReference(1);
    }

    /**
     * Constructor that creates substream from other ASMemoryInStream. Note that
     * no buffer copy is performed.
     *
     * @param stream is stream, from which substream will be taken.
     * @param offset is beginning of data to copy.
     * @param length is length of data to copy.
     */
    public ASMemoryInStream(ASMemoryInStream stream, int offset, int length) {
        this.buffer = stream.buffer;
        this.copiedBuffer = false;
        this.bufferOffset = offset + stream.bufferOffset;
        if (this.bufferOffset < 0) {
            throw new IllegalArgumentException("Range preceeds start of buffer");
        }
        if (this.bufferOffset + length > stream.bufferSize) {
            throw new IllegalArgumentException("Range exceeds end of buffer");
        }
        this.currentPosition = 0;
        this.resetPosition = this.currentPosition;
        this.bufferSize = Math.min(stream.bufferSize, bufferOffset + length);
        this.numOfBufferUsers = stream.numOfBufferUsers;
        this.numOfBufferUsers.increment();
    }

    /**
     * Constructor from byte array and actual data length. Buffer is copied
     * while initializing ASMemoryInStream.
     *
     * @param buffer     byte array containing data.
     * @param bufferSize actual length of data in buffer.
     */
    public ASMemoryInStream(byte[] buffer, int bufferSize) {
        this(buffer, bufferSize, true);
    }

    /**
     * Constructor from byte array and actual data length. Whether buffer is
     * copied deeply or just reference is copied can be manually set.
     *
     * @param buffer     byte array containing data.
     * @param bufferSize actual length of data in buffer.
     * @param copyBuffer is true if buffer should be copied deeply. Note that if
     *                   it is set into false then internal buffer can be changed
     *                   from outside of this class.
     */
    public ASMemoryInStream(byte[] buffer, int bufferSize, boolean copyBuffer) {
        this.bufferSize = bufferSize;
        this.currentPosition = 0;
        this.copiedBuffer = copyBuffer;
        if (copyBuffer) {
            this.buffer = Arrays.copyOf(buffer, bufferSize);
            this.numOfBufferUsers = new IntReference(1);
        } else {
            this.buffer = buffer;
            this.numOfBufferUsers = new IntReference(2);    // buffer is used somewhere else
        }
    }

    @Override
    public long getCurrentOffset() {
        return currentPosition + bufferOffset;
    }

    /**
     * Reads up to size bytes of data into given array.
     *
     * @param buffer is array into which data is read.
     * @param size   is maximal amount of data that can be read.
     * @return actual amount of bytes reas.
     * @throws IOException
     */
    @Override
    public int read(byte[] buffer, int size) throws IOException {
        if (getCurrentOffset() == bufferSize) {
            return -1;
        }
        int available = Math.min(bufferSize - (int) getCurrentOffset(), size);
        try {
            System.arraycopy(this.buffer, (int) getCurrentOffset(), buffer, 0, available);
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new IOException("Can't write bytes into passed buffer: too small.");
        }
        currentPosition += available;
        return available;
    }

    /**
     * Reads single byte.
     *
     * @return byte read or -1 if end is reached.
     * @throws IOException
     */
    @Override
    public int read() throws IOException {
        if (getCurrentOffset() == bufferSize) {
            return -1;
        }
        return this.buffer[bufferOffset + currentPosition++] & 0xFF;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int peek() throws IOException {
        if (getCurrentOffset() == bufferSize) {
            return -1;
        }
        return this.buffer[(int)getCurrentOffset()] & 0xFF;
    }

    /**
     * Skips up to size bytes of data.
     *
     * @param size is amount of bytes to skip.
     * @return actual amount of bytes skipped.
     * @throws IOException
     */
    @Override
    public int skip(int size) throws IOException {
        int available = Math.min(bufferSize - (int) getCurrentOffset(), size);
        currentPosition += available;
        return available;
    }

    /**
     * Closes stream.
     *
     * @throws IOException
     */
    @Override
    public void closeResource() throws IOException {
        if (!isSourceClosed) {
            isSourceClosed = true;
            this.numOfBufferUsers.decrement();
            if (numOfBufferUsers.equals(0)) {
                buffer = null;
            }
        }
    }

    @Override
    public boolean markSupported() {
        return true;
    }


    @Override
    public synchronized void mark(int readlimit) {
        resetPosition = currentPosition;
    }

    /**
     * Resets stream.
     *
     * @throws IOException
     */
    @Override
    public void reset() throws IOException {
        currentPosition = resetPosition;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getStreamLength() throws IOException {
        return this.bufferSize - this.bufferOffset;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getOffset() throws IOException {
        return this.currentPosition;
    }

    @Override
    public void seek(long offset) throws IOException {
        if (offset < 0 || offset > this.bufferSize) {
            throw new IOException("Can't seek for offset " + offset + " in ASMemoryInStream");
        }
        this.currentPosition = (int) offset;
    }

    /**
     * @return the amount of bytes left in stream.
     */
    @Override
    public int available() {
        return bufferSize - (int) getCurrentOffset();
    }

    /**
     * @return true if internal buffer was copied deeply.
     */
    public boolean isCopiedBuffer() {
        return copiedBuffer;
    }

    @Override
    public ASInputStream getStream(long startOffset, long length) throws IOException {
        if (startOffset > 0 && startOffset < this.bufferSize &&
                startOffset + length <= this.bufferSize) {
            return new ASMemoryInStream(this, (int) startOffset, (int) length);
        }
        throw new IOException();
    }

    @Override
    public SeekableInputStream getSeekableStream(long startOffset, long length) throws IOException {
        return new ASMemoryInStream(this, (int) startOffset, (int) length);
    }
}
