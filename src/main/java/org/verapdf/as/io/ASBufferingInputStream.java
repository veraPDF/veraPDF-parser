package org.verapdf.as.io;

import org.verapdf.as.filters.ASInFilter;
import org.verapdf.as.filters.io.ASBufferingInFilter;

import java.io.IOException;

/**
 * This is buffered class-wrapper around ASInputStream. Buffer allows to
 * implement peek() and unread();
 *
 * @author Sergey Shemyakov
 */
public class ASBufferingInputStream extends ASInFilter {

    private byte[] buffer = new byte[ASBufferingInFilter.BF_BUFFER_SIZE];
    private static final int HALF = ASBufferingInFilter.BF_BUFFER_SIZE / 2;

    // when pos reaches this value buffer should be fed
    private static final int BUFFER_FEED_THRESHOLD = 3 * ASBufferingInFilter.BF_BUFFER_SIZE / 4;

    /*
    pos is a pointer to data that will be read next. Preferably it should not
    differ much from bufferSize / 2. In this case we can easily unread
    bufferSize / 2 bytes and peek for bufferSize / 2 bytes.
     */
    private int pos = 0;
    private int eod = -1;

    private int readCounter = 0;

    /**
     * Constructor that initializes internal buffer. Data is read in the second
     * half of buffer allowing to perform peek() and unread successfully.
     *
     * @param stream
     * @throws IOException
     */
    public ASBufferingInputStream(ASInputStream stream) throws IOException {
        super(stream);
        readFromStreamToBuffer(HALF, HALF);
        pos = HALF;
    }

    @Override
    public int read(byte[] buffer, int size) throws IOException {
        if (pos >= eod) {
            return -1;
        }
        if (size < bufferSize()) {
            int read = copyFromBuffer(pos, size, buffer);
            pos += read;
            if (pos > BUFFER_FEED_THRESHOLD) {
                feedBuffer();
            }
            readCounter += read;
            return read;
        } else {
            int copied = copyFromBuffer(pos, size, buffer);
            int read = getInputStream().read(buffer, copied, size - copied);
            if (read == -1) {
                read = 0;
            }
            // adding read data to buffer
            shiftBuffer(copied + read);
            int sourceBeginOffset = Math.max(0, copied + read - buffer.length);
            int destBeginOffset = Math.max(0, buffer.length - copied - read);
            int readSize = Math.min(buffer.length, copied + read);
            System.arraycopy(buffer, sourceBeginOffset, this.buffer, destBeginOffset,
                    readSize);
            pos = buffer.length;
            feedBuffer();
            readCounter += copied + read;
            return copied + read;
        }
    }

    @Override
    public int read(byte[] buffer) throws IOException {
        return read(buffer, buffer.length);
    }

    @Override
    public int read() throws IOException {
        if (pos >= eod) {
            return -1;
        }
        int res = buffer[pos++];
        if (pos > BUFFER_FEED_THRESHOLD) {
            feedBuffer();
        }
        readCounter++;
        return res;
    }

    public byte readByte() throws IOException {
        return (byte) read();
    }

    @Override
    public int skip(int size) throws IOException {
        byte[] b = new byte[size];
        return this.read(b);
    }

    public byte peek() throws IOException {
        return peek(0);
    }

    public byte peek(int i) throws IOException {
        int index = pos + i;
        if (index > buffer.length || index < 0) {
            throw new IOException("Can't peek at index " + index + " in buffer in ASBufferingInputStream.");
        }
        if (eod != -1 && index > eod) {
            return -1;
        }
        return this.buffer[index];
    }

    public void unread() throws IOException {
        unread(1);
    }

    public void unread(int i) throws IOException {
        int index = pos - i;
        if (index > buffer.length || index < 0) {
            throw new IOException("Can't unread for index " + index + " in buffer in ASBufferingInputStream.");
        }
        this.pos = index;
        this.readCounter -= i;
    }

    public boolean isEOF() {
        return pos > eod;
    }

    public void resetReadCounter() {
        this.readCounter = 0;
    }

    public int getReadCounter() {
        return readCounter;
    }

    private void feedBuffer() throws IOException {
        if (pos > HALF) {
            int bytesToFeed = pos - HALF;
            shiftBuffer(bytesToFeed);
            readFromStreamToBuffer(buffer.length - bytesToFeed, bytesToFeed);
            pos = HALF;
        }
    }

    private void shiftBuffer(int length) {
        if (length > buffer.length) {
            length = buffer.length;
        }
        for (int i = length; i < buffer.length; ++i) {
            buffer[i - length] = buffer[i];
        }
    }

    private int bufferSize() {
        return eod == -1 ? buffer.length - pos : eod - pos;
    }

    private void readFromStreamToBuffer(int offset, int len) throws IOException {
        int read = getInputStream().read(buffer, offset, len);
        if (read < len) {
            // TODO: in fact, read may be less then len even if eof is not reached.
            // Fix problem for this case.
            eod = offset + read;
        }
    }

    private int copyFromBuffer(int offset, int size, byte[] res) {
        int endOfData = eod != -1 ? Math.min(eod, buffer.length) : buffer.length;
        int actualCopy = Math.min(size, endOfData - offset);
        System.arraycopy(buffer, offset, res, 0, actualCopy);
        return actualCopy;
    }
}
