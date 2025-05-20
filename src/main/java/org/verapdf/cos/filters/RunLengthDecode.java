package org.verapdf.cos.filters;

import org.verapdf.as.filters.io.ASBufferedInFilter;
import org.verapdf.as.io.ASInputStream;

import java.io.IOException;

/**
 * @author Maxim Plushchov
 */
public class RunLengthDecode extends ASBufferedInFilter {
    private boolean streamEnded = false;
    private byte[] leftoverBuffer = new byte[0];
    private int leftoverSize = 0;

    public RunLengthDecode(ASInputStream stream) throws IOException {
        super(stream);
    }

    @Override
    public int read(byte[] buffer, int size) throws IOException {
        if (streamEnded) {
            return -1;
        }
        int outputPointer = 0;
        if (leftoverSize > 0) {
            if (leftoverSize >= size) {
                System.arraycopy(leftoverBuffer, leftoverBuffer.length - leftoverSize, buffer, 0, size);
                leftoverSize -= size;
                return size;
            }
            System.arraycopy(leftoverBuffer, leftoverBuffer.length - leftoverSize, buffer, 0, leftoverSize);
            outputPointer = leftoverSize;
            leftoverSize = 0;
        }
        if (this.bufferSize() == 0 && this.feedBuffer(getBufferCapacity()) == -1) {
            this.streamEnded = true;
        }
        while (!streamEnded) {
            if (bufferSize() == 0 && this.feedBuffer(getBufferCapacity()) == -1) {
                this.streamEnded = true;
                break;
            }
            int b = bufferPop();
            if (b >= 0) {
                if (bufferSize() == 0 && this.feedBuffer(getBufferCapacity()) == -1) {
                    this.streamEnded = true;
                    break;
                }
                int count = b + 1;
                byte[] data = new byte[count];
                int read = bufferPopArray(data, count);
                while (read != count) {
                    if (this.feedBuffer(this.getBufferCapacity()) == -1) {
                        this.streamEnded = true;
                        break;
                    }
                    byte[] extraBytes = new byte[count - read];
                    int readAgain = bufferPopArray(extraBytes, extraBytes.length);
                    System.arraycopy(extraBytes, 0, data, read, readAgain);
                    read += readAgain;
                }
                if (streamEnded) {
                    break;
                }
                int leftBufferSize = size - outputPointer;
                if (count > leftBufferSize) {
                    System.arraycopy(data, 0, buffer, outputPointer, leftBufferSize);
                    leftoverSize = count - leftBufferSize;
                    leftoverBuffer = new byte[leftoverSize];
                    System.arraycopy(data, leftBufferSize, leftoverBuffer, 0, leftoverSize);
                    return size;
                }
                System.arraycopy(data, 0, buffer, outputPointer, count);
                outputPointer += count;
            } else {
                int runLength = -b + 1;
                if (bufferSize() == 0 && this.feedBuffer(getBufferCapacity()) == -1) {
                    this.streamEnded = true;
                    break;
                }
                byte value = bufferPop();
                int leftBufferSize = size - outputPointer;
                for (int i = 0; i < leftBufferSize; i++) {
                    buffer[outputPointer++] = value;
                }
                if (runLength > leftBufferSize) {
                    leftoverSize = runLength - leftBufferSize;
                    leftoverBuffer = new byte[leftoverSize];
                    for (int i = 0; i < leftoverSize; i++) {
                        buffer[outputPointer++] = value;
                    }
                    return size;
                }
            }
        }
        return outputPointer;
    }
}
