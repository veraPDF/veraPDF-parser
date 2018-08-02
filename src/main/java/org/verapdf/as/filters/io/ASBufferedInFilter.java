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
import org.verapdf.as.io.ASMemoryInStream;
import org.verapdf.parser.NotSeekableBaseParser;

import java.io.IOException;
import java.util.Arrays;

/**
 * Class provides buffered input from input stream.
 * It has two uses. If the ASBufferedInFilter object is used as a buffered
 * stream (e. g. in unseekable parsers) then the buffer holds DECODED bytes read
 * from inlaying stream.
 * Before using the ASBufferedInFilter in this make sure to call initialize()
 * method.
 *
 * In filter classes that are inherited from ASBufferedInFilter the buffer holds
 * ENCODED bytes that are processed into decoded bytes on read() method calls.
 *
 * @author Sergey Shemyakov
 */
public class ASBufferedInFilter extends ASInFilter {

    public static final int START_BUFFER_SIZE = 10240;
    public static final int BF_BUFFER_SIZE = 2048;

    private final int HALF;

    // when pos reaches this value buffer should be fed
    private final int BUFFER_FEED_THRESHOLD;

    /*
    pos is a pointer to data that will be read next. Preferably it should not
    differ much from bufferSize / 2. In this case we can easily unread
    bufferSize / 2 bytes and peek for bufferSize / 2 bytes.
     */
    private int pos = 0;
    private int eod = -1;

    private boolean initialized = false;

    private int readCounter = 0;

    private final int bufferCapacity;
    protected byte[] buffer;
    private int bufferBegin, bufferEnd;

    public ASBufferedInFilter(ASInputStream stream) throws IOException {
        this(stream, BF_BUFFER_SIZE);
    }

    public ASBufferedInFilter(ASInputStream stream, int buffCapacity) {
        super(stream);
        this.bufferCapacity = buffCapacity;
        buffer = new byte[buffCapacity];
        bufferEnd = bufferBegin = 0;
        this.HALF = buffCapacity / 2;
        this.BUFFER_FEED_THRESHOLD = 3 * buffCapacity / 4;
    }

    /**
     * This method should be called before using ASBufferedInFilter as buffered
     * stream.
     */
    public void initialize() throws IOException {
        this.initialized = true;
        readFromStreamToBuffer(HALF, HALF);
        pos = HALF;
    }

    @Override
    public int read(byte[] buffer, int size) throws IOException {
        if (eod != -1 && pos >= eod) {
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
            int sourceBeginOffset = Math.max(0, copied + read - this.buffer.length);
            int destBeginOffset = Math.max(0, this.buffer.length - copied - read);
            int readSize = Math.min(buffer.length, copied + read);
            System.arraycopy(buffer, sourceBeginOffset, this.buffer, destBeginOffset,
                    readSize);
            pos = this.buffer.length;
            feedBuffer();
            readCounter += copied + read;
            return copied + read;
        }
    }

    @Override
    public int read(byte[] buffer) throws IOException {
        return read(buffer, buffer.length);
    }

    public byte readByte() throws IOException {
        if (eod != -1 && pos >= eod) {
            return -1;
        }
        int res = buffer[pos++];
        if (pos > BUFFER_FEED_THRESHOLD && eod == -1) {
            feedBuffer();
        }
        readCounter++;
        return (byte) res;
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
        long actuallyRead = this.getInputStream().read(buffer, bytesToRead);
        bufferBegin = 0;
        bufferEnd = (int) actuallyRead;
        return actuallyRead;
    }

    private void feedBuffer() throws IOException {
        if (pos > HALF) {
            int bytesToFeed = pos - HALF;
            shiftBuffer(bytesToFeed);
            readFromStreamToBuffer(buffer.length - bytesToFeed, bytesToFeed);
        }
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
        System.arraycopy(toAdd, 0, this.buffer, bufferEnd, bytesToAdd);
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
        return buffer[bufferBegin++];
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
        System.arraycopy(this.buffer, bufferBegin, buffer, 0, actualRead);
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
        this.buffer = new byte[this.bufferCapacity];
        bufferEnd = bufferBegin = pos = readCounter = 0;
        eod = -1;
        if (initialized) {
            initialize();
        }
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

    public byte peek() throws IOException {
        return peek(0);
    }

    public byte peek(int i) throws IOException {
        int index = pos + i;
        if (index > buffer.length || index < 0) {
            throw new IOException("Can't peek at index " + index + " in buffer in ASBufferingInputStream.");
        }
        if (eod != -1 && index >= eod) {
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
        return eod != -1 && pos >= eod;
    }

    public void resetReadCounter() {
        this.readCounter = 0;
    }

    public int getReadCounter() {
        return readCounter;
    }

    private void readFromStreamToBuffer(int offset, int len) throws IOException {
        int read = getInputStream().read(buffer, offset, len);
        this.bufferEnd = read;
        if (read < len) {
            // TODO: in fact, read may be less then len even if eof is not reached.
            // Fix problem for this case.
            eod = offset + (read == -1 ? 0 : read);
        }
    }

    private int copyFromBuffer(int offset, int size, byte[] res) {
        int endOfData = eod != -1 ? Math.min(eod, buffer.length) : buffer.length;
        int actualCopy = Math.min(size, endOfData - offset);
        System.arraycopy(buffer, offset, res, 0, actualCopy);
        return actualCopy;
    }

    private void shiftBuffer(int length) {
        if (length > buffer.length) {
            length = buffer.length;
        }
        for (int i = length; i < buffer.length; ++i) {
            buffer[i - length] = buffer[i];
        }
        this.pos -= length;
        if (this.eod != -1) {
            this.eod -= length;
        }
    }

    /**
     * Gets a stream that is a piece of this stream. The next length bytes will
     * be the data in the new stream.
     *
     * @param length is the length of new stream.
     * @return new stream.
     */
    public ASInputStream getStream(int length) throws IOException {
        byte[] buf = new byte[START_BUFFER_SIZE];
        int pointer = 0;
        byte readByte = this.readByte();
        while (pointer < length && !this.isEOF()) {
            buf[pointer++] = readByte;
            if (pointer == buf.length) {
                buf = NotSeekableBaseParser.extendArray(buf);
            }
            readByte = this.readByte();
        }
        return new ASMemoryInStream(buf, buf.length, false);
    }

    /**
     * Gets a stream that is a piece of this stream. The data is taken from the
     * current buffer position until the given token is not found.
     *
     * @param token is the byte array that means the end of stream.
     * @return new stream.
     */
    public ASInputStream getStreamUntilToken(byte[] token) throws IOException {
        byte[] buf = new byte[START_BUFFER_SIZE];
        int read = this.read(buf, token.length);
        byte readByte = this.readByte();
        int pointer = read;
        if (pointer != token.length) {
            throw new IOException("Stream is shorter than finishing token");
        }
        while (!this.isEOF() && !Arrays.equals(token, Arrays.copyOfRange(buf, pointer - token.length, pointer))) {
            buf[pointer++] = readByte;
            if (pointer == buf.length) {
                buf = NotSeekableBaseParser.extendArray(buf);
            }
            readByte = this.readByte();
        }
        return new ASMemoryInStream(buf, buf.length, false);
    }
}
