package org.verapdf.io;

import java.io.Closeable;
import java.io.IOException;

/**
 * Represents stream in which seek for a particular byte offset can be performed.
 * On creation, contents of this stream should be written into file or memory
 * buffer.
 *
 * @author Sergey Shemyakov
 */
public interface SeekableStream extends Closeable {

    /**
     * Reads next byte from stream.
     *
     * @return byte read.
     */
    int read() throws IOException;

    /**
     * Reads particular amount of bytes into buffer.
     *
     * @param buf  is buffer to read into.
     * @param size amount of bytes to read.
     * @return actual amount of read bytes.
     */
    int read(byte[] buf, int size) throws IOException;

    /**
     * Goes to a particular byte in stream.
     *
     * @param offset is offset of a byte to go to.
     */
    void seek(long offset) throws IOException;

    /**
     * Skips bytes in stream without reading them.
     *
     * @param size is amount of bytes to skip.
     * @return amount of actually skipped bytes.
     */
    int skip(int size) throws IOException;

    /**
     * Resets this stream.
     */
    void reset() throws IOException;

    /**
     * Gets offset of current byte.
     *
     * @return offset of byte to be read next.
     */
    long getOffset() throws IOException;

    /**
     * Gets total length of stream.
     *
     * @return length of stream in bytes.
     */
    long getLength() throws IOException;

    /**
     * Gets next byte without reading it.
     *
     * @return next byte.
     */
    int peek() throws IOException;
}
