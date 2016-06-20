package org.verapdf.cos;

import org.verapdf.as.filters.io.ASBufferingInFilter;
import org.verapdf.as.filters.io.COSFilterASCIIReader;
import org.verapdf.as.io.ASInputStream;

import java.io.IOException;

/**
 * @author Sergey Shemyakov
 */
public class COSFilterASCII85Decode extends ASBufferingInFilter {

    /**
     * Constructor from encoded stream.
     *
     * @param stream is ASCII85 encoded stream.
     * @throws IOException
     */
    public COSFilterASCII85Decode(ASInputStream stream) throws IOException {
        super(stream);
    }

    /**
     * Reads up to size bytes of ASCII85 decoded data into buffer.
     *
     * @param buffer is byte array where decoded data will be read.
     * @param size   is maximal amount of decoded bytes.
     * @return amount of actually read bytes.
     * @throws IOException
     */
    @Override
    public int read(byte[] buffer, int size) throws IOException {
        return super.read(buffer, size);
    }

    @Override
    protected void decode() throws IOException { // TODO: add here checking of size of decoded data and, possibly, decoding into file.
        byte[] decodedBuffer = new byte[(BF_BUFFER_SIZE / 4) * 4];  // Guarantees that size of this buffer is divided by 4
        byte[] decodedData = new byte[0];
        int decodedBufferPointer = 0;
        COSFilterASCIIReader reader =
                new COSFilterASCIIReader(this.getInputStream(), false);
        byte[] fiveBytes = reader.getNextBytes();
        byte[] decodedBytes = new byte[4];
        while (fiveBytes != null) {
            if(fiveBytes.length == 5) {
                decodeFiveBytes(fiveBytes, decodedBytes);
            }

        }
    }

    private void decodeFiveBytes(byte[] fiveBytes, byte[] fourBytes) {
        long value = 0;
        for(int i = 0; i < 5; ++i) {
            value += fiveBytes[i] - '!';
            value *= 85;
        }
        value /= 85;
        for(int i = 3; i >= 0; i--) {
            fourBytes[i] = (byte) (value % 256);
            value /= 256;
        }
    }

    private int concatenateFourBytes(byte[] decodedBuffer,
                                      int decodedBufferPointer,
                                      byte[] decodedData) {
        if(decodedBuffer.length - decodedBufferPointer < 4) {

        }
    }
}
