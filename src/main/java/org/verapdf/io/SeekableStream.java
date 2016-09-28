package org.verapdf.io;

import org.verapdf.as.filters.io.ASBufferingInFilter;
import org.verapdf.as.io.ASInputStream;
import org.verapdf.as.io.ASMemoryInStream;

import java.io.IOException;
import java.io.InputStream;

/**
 * Represents stream in which seek for a particular byte offset can be performed.
 * On creation, contents of this stream should be written into file or memory
 * buffer.
 *
 * @author Sergey Shemyakov
 */
public abstract class SeekableStream extends ASInputStream {

    public static final int MAX_BUFFER_SIZE = 10240;

    /**
     * Goes to a particular byte in stream.
     *
     * @param offset is offset of a byte to go to.
     */
    public abstract void seek(long offset) throws IOException;

    /**
     * Gets offset of current byte.
     *
     * @return offset of byte to be read next.
     */
    public abstract long getOffset() throws IOException;

    /**
     * Gets total length of stream.
     *
     * @return length of stream in bytes.
     */
    public abstract long getStreamLength() throws IOException;

    /**
     * Gets next byte without reading it.
     *
     * @return next byte.
     */
    public abstract int peek() throws IOException;

    /**
     * Gets substream of this stream that starts at given offset and has given
     * length.
     *
     * @param startOffset is starting offset of substream.
     * @param length is length of substream.
     */
    public abstract ASInputStream getStream(long startOffset, long length) throws IOException;

    /**
     * @return true if end of stream is reached.
     */
    public boolean isEOF() throws IOException {
        return this.getOffset() == this.getStreamLength();
    }

    public void unread() throws IOException {
        this.seek(this.getOffset() - 1);
    }


    public void unread(final int count) throws IOException {
        this.seek(this.getOffset() - count);
    }

    public void seekFromCurrentPosition(final long pos) throws IOException {
        this.seek(getOffset() + pos);
    }

    public void seekFromEnd(final long pos) throws IOException {
        final long size = this.getStreamLength();
        this.seek(size - pos);
    }

    public byte readByte() throws IOException {
        int next = this.read();
        if(next < 0) {
            throw new IOException("End of file is reached");
        }
        return (byte) next;
    }

    /**
     * Returns InternalInputStream or ASMemoryInStream constructed from given
     * stream depending on stream length.
     *
     * @param stream is stream to turn into seekable stream.
     * @return SeekableStream that contains data of passed stream.
     */
    public static SeekableStream getSeekableStream(InputStream stream) throws IOException {
        int totalRead = 0;
        byte[] buffer = new byte[0];
        byte[] temp = new byte[ASBufferingInFilter.BF_BUFFER_SIZE];
        int read = stream.read(temp);
        do {
            buffer = ASBufferingInFilter.concatenate(buffer, buffer.length, temp, read);
            totalRead += read;
            read = stream.read(temp);
            if(read == -1) {
                return new ASMemoryInStream(buffer, buffer.length, false);
            }
        } while (totalRead < MAX_BUFFER_SIZE);
        return new InternalInputStream(stream);
    }
}
