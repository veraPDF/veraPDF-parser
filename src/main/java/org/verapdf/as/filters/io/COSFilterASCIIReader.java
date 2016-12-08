package org.verapdf.as.filters.io;

import org.verapdf.as.io.ASInputStream;

import java.io.IOException;
import java.util.Arrays;

/**
 * This is class-helper for decoding ASCII85 and ASCII Hex strings. It reads
 * next required amount of bytes and guarantees that white-space characters will
 * be skipped. It also looks for end of data.
 *
 * @author Sergey Shemyakov
 */
public class COSFilterASCIIReader {

    private boolean isASCIIHex;
    private ASInputStream stream;
    private byte[] buf;
    private int bufPointer;
    private static final byte ASCII_HEX_EOD = '>';
    private static final byte ASCII85_EOD = '~';
    private static final byte Z = 'z';
    private static final byte EXCLAM_MARK = '!';
    private boolean isEOD;
    private int ascii85ZeroRemains;

    /**
     * Constructor from encoded stream.
     *
     * @param stream     is ASCII Hex or ASCII85 encoded stream.
     * @param isASCIIHex is true if stream ASCII Hex encoded, false if stream is
     *                   ASCII85 encoded.
     * @throws IOException
     */
    public COSFilterASCIIReader(ASInputStream stream, boolean isASCIIHex) throws IOException {
        this.stream = stream;
        this.isASCIIHex = isASCIIHex;
        this.buf = new byte[ASBufferingInFilter.BF_BUFFER_SIZE];
        bufPointer = 0;
        if(buf[0] == '<' && buf[1] == '~') {    //Skipping leading <~
            bufPointer += 2;
        }
        isEOD = false;
        ascii85ZeroRemains = 0;
    }

    /**
     * Method gets next portion of bytes: two for ASCII Hex encoding and five
     * for ASCII85 encoding. It ignores all the white-space characters.
     *
     * @return null if end of stream is reached and next portion of bytes
     * otherwise. If ASCII85 is chosen, method can return less than 5 bytes in
     * case if they are last group of bytes in data string.
     * @throws IOException
     */
    public byte[] getNextBytes() throws IOException {
        if (isEOD) {
            return null;
        }
        if (isASCIIHex) {
            byte[] twoBytes = new byte[2];
            byte b;
            do {
                b = readByte();
            } while (isWS(b) && b != -1);
            if (b == -1 || b == ASCII_HEX_EOD) {
                isEOD = true;
                return null;
            } else {
                if (!isValidASCIIHexByte(b)) {
                    throw new IOException("Can not read ASCII Hex string.");
                }
                twoBytes[0] = b;
            }
            do {
                b = readByte();
            } while (isWS(b) && b != -1);
            if (b == -1 || b == ASCII_HEX_EOD) {
                isEOD = true;
                twoBytes[1] = 0;
            } else {
                if (!isValidASCIIHexByte(b)) {
                    throw new IOException("Can not read ASCII Hex string.");
                }
                twoBytes[1] = b;
            }
            return twoBytes;
        } else {
            byte[] fiveBytes = new byte[5];
            for (int i = 0; i < 5; i++) {
                fiveBytes[i] = 0;
            }
            for (int i = 0; i < ascii85ZeroRemains; ++i) {
                fiveBytes[i] = '!';
            }
            byte b;
            if (ascii85ZeroRemains == 0) {
                do {
                    b = readByte();
                } while (isWS(b) && b != -1);
                if (b == -1 || b == ASCII85_EOD) {
                    isEOD = true;
                    return null;
                } else if (b == Z) {
                    processCaseOfZ(fiveBytes, 0);
                    return fiveBytes;
                } else {
                    if (!isValidASCII85Byte(b)) {
                        throw new IOException("Can not read ASCII85 string.");
                    }
                    fiveBytes[0] = b;
                }
            }
            for (int i = ascii85ZeroRemains == 0 ? 1 : ascii85ZeroRemains; i < 5; ++i) {
                do {
                    b = readByte();
                } while (isWS(b) && b != -1);
                if (b == -1 || b == ASCII85_EOD) {
                    isEOD = true;
                    return Arrays.copyOf(fiveBytes, i);
                } else if (b == Z) {
                    processCaseOfZ(fiveBytes, i);
                    return fiveBytes;
                } else {
                    if (!isValidASCII85Byte(b)) {
                        throw new IOException("Can not read ASCII85 string.");
                    }
                    fiveBytes[i] = b;
                }
            }
            ascii85ZeroRemains = 0;
            return fiveBytes;
        }
    }

    private static boolean isWS(byte c) {
        return c == 0 || c == 9 || c == 10 || c == 12 || c == 13 || c == 32;
    }

    private static boolean isValidASCIIHexByte(byte c) {
        return (c >= '0' && c <= '9') ||
                (c >= 'a' && c <= 'f') ||
                (c >= 'A' && c <= 'F');
    }

    private static boolean isValidASCII85Byte(byte c) {
        return (c >= '!' && c <= 'u') || c == 'z';
    }

    private byte readByte() throws IOException {
        if (bufPointer == buf.length) {
            if (this.stream.read(buf, buf.length) == -1) {
                return -1;
            }
            bufPointer = 0;
        }
        return buf[bufPointer++];
    }

    private void processCaseOfZ(byte[] fiveBytes, int i) {  
        for(int j = i; j < 5; ++j) {
            fiveBytes[j] = EXCLAM_MARK;
        }
        ascii85ZeroRemains = i;
    }
}
