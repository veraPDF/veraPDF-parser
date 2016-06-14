package org.verapdf.as.io;

import java.util.Arrays;

/**
 * @author Sergey Shemyakov
 */
public class ASMemoryInStream extends ASInputStream {

    private long bufferSize;
    private long currentPosition;
    private byte[] buffer;

    public ASMemoryInStream(byte[] buffer, long bufferSize) {
        this(buffer, bufferSize, true);
    }

    public ASMemoryInStream(byte[] buffer, long bufferSize, boolean copyBuffer) {
        this.bufferSize = bufferSize;
        this.currentPosition = 0;
        if(copyBuffer) {
            this.buffer = Arrays.copyOf(buffer, (int) bufferSize);
        } else {
            this.buffer = buffer;
        }
    }


}
