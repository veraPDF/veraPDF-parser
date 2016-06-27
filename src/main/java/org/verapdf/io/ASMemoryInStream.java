package org.verapdf.io;

import org.verapdf.as.io.ASInputStream;

import java.io.IOException;
import java.util.Arrays;

/**
 * This class binds the ASInputStream interface to a memory buffer.
 *
 * @author Sergey Shemyakov
 */
public class ASMemoryInStream implements ASInputStream {

    private int bufferSize;
    private int currentPosition;
    private byte[] buffer;
    private boolean copiedBuffer;

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
     *                   it is set into false, internal buffer can be changed
     *                   from outside of this class.
     */
    public ASMemoryInStream(byte[] buffer, int bufferSize, boolean copyBuffer) {
        this.bufferSize = bufferSize;
        this.currentPosition = 0;
        this.copiedBuffer = copyBuffer;
        if (copyBuffer) {
            this.buffer = Arrays.copyOf(buffer, bufferSize);
        } else {
            this.buffer = buffer;
        }
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
        if (currentPosition == bufferSize) {
            return -1;
        }
        int available = Math.min(bufferSize - currentPosition, size);
        System.arraycopy(this.buffer, currentPosition, buffer, 0, available);
        currentPosition += available;
        return available;
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
        int available = Math.min(bufferSize - currentPosition, size);
        currentPosition += available;
        return available;
    }

    /**
     * Closes stream.
     *
     * @throws IOException
     */
    @Override
    public void close() throws IOException {
        bufferSize = 0;
        currentPosition = 0;
        buffer = null;
    }

    /**
     * Resets stream.
     *
     * @throws IOException
     */
    @Override
    public void reset() throws IOException {
        currentPosition = 0;
    }

    /**
     * @return true if internal buffer was copied deeply.
     */
    public boolean isCopiedBuffer() {
        return copiedBuffer;
    }
}
