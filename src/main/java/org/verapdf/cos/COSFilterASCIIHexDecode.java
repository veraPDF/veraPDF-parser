package org.verapdf.cos;

import org.verapdf.as.filters.io.ASBufferingInFilter;
import org.verapdf.as.filters.io.COSFilterASCIIReader;
import org.verapdf.as.io.ASInputStream;
import org.verapdf.io.ASMemoryInStream;

import java.io.IOException;

/**
 * @author Timur Kamalov
 */
public class COSFilterASCIIHexDecode extends ASBufferingInFilter {

    public final static byte ws = 17;
    public final static byte er = 127;

    private final static byte[] loHexTable = {
            ws, er, er, er, er, er, er, er, er, ws, ws, er, ws, ws, er, er,    // 0  - 15
            er, er, er, er, er, er, er, er, er, er, er, er, er, er, er, er,    // 16 - 31
            ws, er, er, er, er, er, er, er, er, er, er, er, er, er, er, er,    // 32 - 47
            0, 1, 2, 3, 4, 5, 6, 7, 8, 9, er, er, er, er, er, er,    // 48 - 63
            er, 10, 11, 12, 13, 14, 15, er, er, er, er, er, er, er, er, er,    // 64 - 79
            er, er, er, er, er, er, er, er, er, er, er, er, er, er, er, er,    // 80 - 95
            er, 10, 11, 12, 13, 14, 15, er, er, er, er, er, er, er, er, er,    // 96 - 111
            er, er, er, er, er, er, er, er, er, er, er, er, er, er, er, er,    // 112 - 127
            er, er, er, er, er, er, er, er, er, er, er, er, er, er, er, er,    // 128 - 143
            er, er, er, er, er, er, er, er, er, er, er, er, er, er, er, er,    // 144 - 159
            er, er, er, er, er, er, er, er, er, er, er, er, er, er, er, er,    // 160 - 175
            er, er, er, er, er, er, er, er, er, er, er, er, er, er, er, er,    // 176 - 191
            er, er, er, er, er, er, er, er, er, er, er, er, er, er, er, er,    // 192 - 207
            er, er, er, er, er, er, er, er, er, er, er, er, er, er, er, er,    // 208 - 223
            er, er, er, er, er, er, er, er, er, er, er, er, er, er, er, er,    // 224 - 239
            er, er, er, er, er, er, er, er, er, er, er, er, er, er, er, er    // 240 - 255
    };

    /**
     * Constructor from encoded stream.
     * @param stream is ASCII Hex encoded stream.
     * @throws IOException
     */
    public COSFilterASCIIHexDecode(ASInputStream stream) throws IOException {
        super(stream);
    }

    /**
     * Reads up to size bytes of ASCII Hex decoded data into buffer.
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

    public static byte decodeLoHex(byte val) {
        return loHexTable[val];
    }

    @Override
    protected void decode() throws IOException {    // TODO: add here checking of size of decoded data and, possibly, decoding into file.
        byte[] decodedBuffer = new byte[BF_BUFFER_SIZE];
        byte[] decodedData = new byte[0];
        int decodedDataPointer = 0;
        COSFilterASCIIReader reader =
                new COSFilterASCIIReader(this.getInputStream(), true);
        byte[] twoBytes = reader.getNextBytes();

        byte res;

        while(twoBytes != null) {
            res = (byte) (decodeLoHex(twoBytes[0]) * 16);
            res += decodeLoHex(twoBytes[1]);
            decodedBuffer[decodedDataPointer++] = res;
            if(decodedDataPointer == decodedBuffer.length) {
                concatenate(decodedData, decodedData.length, decodedBuffer, decodedDataPointer);
                decodedDataPointer = 0;
            }
            twoBytes = reader.getNextBytes();
        }
        concatenate(decodedData, decodedData.length, decodedBuffer, decodedDataPointer);
        this.setInputStream(new ASMemoryInStream(decodedData, decodedData.length, false));
    }
}
