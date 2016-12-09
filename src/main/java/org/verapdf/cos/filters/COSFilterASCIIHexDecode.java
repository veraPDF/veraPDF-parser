package org.verapdf.cos.filters;

import org.verapdf.as.filters.io.ASBufferingInFilter;
import org.verapdf.as.filters.io.COSFilterASCIIReader;
import org.verapdf.as.io.ASInputStream;

import java.io.IOException;

/**
 * @author Timur Kamalov
 */
public class COSFilterASCIIHexDecode extends ASBufferingInFilter {

    public final static byte ws = 17;
    public final static byte er = 127;
    COSFilterASCIIReader reader;

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
        reader = new COSFilterASCIIReader(stream, true);
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
        int pointer = 0;
        byte[] twoBytes = reader.getNextBytes();
        byte res;
        for(int i = 0; i < size - 1; ++i) {
            if(twoBytes == null) {
                return pointer == 0 ? -1 : pointer;
            }
            res = (byte) (decodeLoHex(twoBytes[0]) << 4);
            res += decodeLoHex(twoBytes[1]);
            buffer[pointer++] = res;
            twoBytes = reader.getNextBytes();
        }
        if (pointer == 0) {
            return -1;
        }
        res = (byte) (decodeLoHex(twoBytes[0]) << 4);
        res += decodeLoHex(twoBytes[1]);
        buffer[pointer++] = res;
        return pointer == 0 ? -1 : pointer;
    }

    public static byte decodeLoHex(byte val) {
        return loHexTable[val & 0xFF];
    }
}
