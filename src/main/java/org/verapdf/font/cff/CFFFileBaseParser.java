package org.verapdf.font.cff;

import org.verapdf.as.io.ASInputStream;

import java.io.IOException;

/**
 * This class does low-level parsing of CFF file.
 *
 * @author Sergey Shemyakov
 */
public class CFFFileBaseParser {

    private byte offSize;
    private byte hdrSize;
    private ASInputStream source;
    private double[] fontMatrix = {0.001, 0, 0, 0.001, 0, 0};

    CFFFileBaseParser(ASInputStream source) {
        this.source = source;
    }

    protected byte readCard8() throws IOException {
        byte[] buf = new byte[1];
        if (source.read(buf, 1) != 1) {
            throw new IOException("End of stream is reached");
        }
        return buf[0];
    }

    protected int readCard16() throws IOException {
        byte[] buf = new byte[2];
        if (source.read(buf, 2) != 2) {
            throw new IOException("End of stream is reached");
        }
        return (buf[0] << 8) | buf[1];
    }

    protected int readOffset() throws IOException {
        return this.readOffset(this.offSize);
    }

    protected int readOffset(int offSize) throws IOException {
        byte[] buf = new byte[offSize];
        if (source.read(buf, offSize) != offSize) {
            throw new IOException("End of stream is reached");
        }
        int res = 0;
        for (int i = 0; i < offSize - 1; ++i) {
            res |= buf[i];
            res <<= 8;
        }
        res |= buf[offSize - 1];
        return res;
    }

    protected Index readIndex() throws IOException {
        int count = readCard16();
        if (count == 0) {
            return new Index(0, new int[0], new byte[0]);
        }
        byte offSize = readCard8();
        int[] offset = new int[count + 1];
        for (int i = 0; i < count + 1; ++i) {
            offset[i] = readOffset(offSize);
        }
        byte[] data = new byte[offset[count]];
        if (source.read(data, data.length) != data.length) {
            throw new IOException("End of stream is reached");
        }
        return new Index(count, offset, data);
    }

    protected void readHeader() throws IOException {
        readCard8();
        readCard8();
        hdrSize = readCard8();
        this.offSize = readCard8();
    }

    protected double readReal() throws IOException {
        StringBuilder builder = new StringBuilder();
        byte buf;
        parsing:
        while (true) {
            buf = readCard8();
            int[] hexs = new int[2];
            hexs[0] = buf >> 4;
            hexs[1] = buf & 0x0F;
            for (int i = 0; i < 2; ++i) {
                if (hexs[i] < 10) {
                    builder.append(hexs[i]);
                } else {
                    switch (hexs[i]) {
                        case 0x0A:
                            builder.append('.');
                            break;
                        case 0x0B:
                            builder.append('E');
                            break;
                        case 0x0C:
                            builder.append("E-");
                            break;
                        case 0x0E:
                            builder.append('-');
                            break;
                        case 0x0F:
                            break parsing;
                    }
                }
            }
        }
        return Double.parseDouble(builder.toString());
    }

    protected int readInteger(byte firstByte) throws IOException {
        int firstByteValue = firstByte & 0xFF;
        if (firstByteValue > 31 && firstByteValue < 247) {
            return firstByteValue - 139;
        }
        if (firstByteValue > 246 && firstByteValue < 251) {
            int first = (firstByteValue - 247) << 8;
            return first + readCard8() + 108;
        }
        if (firstByteValue > 250 && firstByteValue < 255) {
            int first = (firstByteValue - 251) << 8;
            return -first - readCard8() - 108;
        }
        if (firstByteValue == 28) {
            return readCard16();
        }
        if (firstByteValue == 29) {
            return (readCard16() << 16) | readCard16();
        } else {    // Shouldn't be reached
            throw new IOException("Can't read integer");
        }
    }
}
