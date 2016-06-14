package org.verapdf.as.filters.io;

import org.verapdf.as.filters.ASInFilter;
import org.verapdf.as.io.ASInputStream;

import java.io.IOException;
import java.util.Arrays;

/**
 * Class provides buffered input from input stream.
 * @author Sergey Shemyakov
 */
public class ASBufferingInFilter extends ASInFilter {

    public static final int BF_BUFFER_SIZE = 4096;

    private int bufferCapacity;
    protected byte [] internalBuffer;
    private int bufferBegin, bufferEnd;

    public ASBufferingInFilter(ASInputStream stream) {
        this(stream, BF_BUFFER_SIZE);
    }

    public ASBufferingInFilter(ASInputStream stream, int buffCapacity) {
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
        if(this.bufferSize() > 0) {
            internalBuffer = Arrays.copyOfRange(filter.internalBuffer,
                    filter.bufferBegin, filter.bufferEnd);
        }
    }

    /**
     * Shifts begin marker by up to bytesToProceed bytes to the right of to the
     * end of the buffer if bytesToProceed is too big.
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
     * @param bytesToRead amount of bytes to read.
     * @return amount of bytes actually placed into buffer.
     */
    public long feedBuffer(int bytesToRead) throws IOException {
        bytesToRead = Math.min(bytesToRead, bufferCapacity);
        long actuallyRead = this.getInputStream().read(internalBuffer, bytesToRead);
        bufferBegin = 0;
        bufferEnd = (int) actuallyRead;
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
     * @return character, pointed by buffer begin marker.
     */
    public byte bufferPop() {
        return internalBuffer[bufferBegin++];
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
    public void close() throws IOException {
        super.close();
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
}
