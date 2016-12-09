package org.verapdf.cos.filters;

import org.verapdf.as.filters.io.ASBufferingInFilter;
import org.verapdf.as.filters.io.COSFilterASCIIReader;
import org.verapdf.as.io.ASInputStream;

import java.io.IOException;

/**
 * @author Sergey Shemyakov
 */
public class COSFilterASCII85Decode extends ASBufferingInFilter {

    private COSFilterASCIIReader reader;

    /**
     * Constructor from encoded stream.
     *
     * @param stream is ASCII85 encoded stream.
     * @throws IOException
     */
    public COSFilterASCII85Decode(ASInputStream stream) throws IOException {
        super(stream);
        reader = new COSFilterASCIIReader(stream, false);
    }

    /**
     * Reads up to size bytes of ASCII85 decoded data into buffer.
     *
     * @param buffer is byte array where decoded data will be read.
     * @param size   is maximal amount of decoded bytes. It should be at least 4.
     * @return amount of actually read bytes.
     * @throws IOException
     */
    @Override
    public int read(byte[] buffer, int size) throws IOException {
        int pointer = 0;
        byte[] fiveBytes = reader.getNextBytes();
        byte[] fourBytes = new byte[4];
        while (pointer + 4 <= size) {
            if(fiveBytes == null) {
                break;
            }
            int decoded = decodeFiveBytes(fiveBytes, fourBytes);
            System.arraycopy(fourBytes, 0, buffer, pointer, decoded);
            pointer += decoded;
            fiveBytes = reader.getNextBytes();
        }
        return pointer == 0 ? -1 : pointer;
    }

    private int decodeFiveBytes(byte[] fiveBytes, byte[] fourBytes) {
        long value = 0;
        for(int i = 0; i < fiveBytes.length; ++i) {
            value += fiveBytes[i] - '!';
            value *= 85;
        }
        for(int i = 0; i < 5 - fiveBytes.length; ++i) { // This processes situation of last portion of bytes that can have length != 5.
            value += 'u' - '!';
            value *= 85;
        }
        value /= 85;
        for(int i = 3; i >= 0; i--) {
            fourBytes[i] = (byte) (value % 256);
            value >>= 8;
        }
        return fiveBytes.length - 1;
    }
}
