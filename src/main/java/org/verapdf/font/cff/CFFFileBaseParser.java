package org.verapdf.font.cff;

import org.verapdf.as.io.ASInputStream;
import org.verapdf.font.cff.predefined.CFFStandardStrings;
import org.verapdf.io.InternalInputStream;

import java.io.IOException;

/**
 * This class does low-level parsing of CFF file.
 *
 * @author Sergey Shemyakov
 */
public class CFFFileBaseParser {

    private byte offSize;
    private byte hdrSize;
    protected InternalInputStream source;
    protected CFFIndex definedNames;

    CFFFileBaseParser(ASInputStream source) throws IOException {
        this.source = new InternalInputStream(source);
    }

    protected byte readCard8() throws IOException {
        return this.source.read();
    }

    protected int readCard16() throws IOException {
        return (this.source.read() << 8) | this.source.read();
    }

    protected int readOffset() throws IOException {
        return this.readOffset(this.offSize);
    }

    protected int readOffset(int offSize) throws IOException {
        int res = 0;
        for (int i = 0; i < offSize - 1; ++i) {
            res |= this.source.read();
            res <<= 8;
        }
        res |= this.source.read();
        return res;
    }

    protected CFFIndex readIndex() throws IOException {
        int count = readCard16();
        if (count == 0) {
            return new CFFIndex(0, new int[0], new byte[0]);
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
        return new CFFIndex(count, offset, data);
    }

    protected void readHeader() throws IOException {
        readCard8();
        readCard8();
        hdrSize = readCard8();
        this.offSize = readCard8();
        this.source.seek(hdrSize);
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

    protected CFFNumber readNumber() throws IOException {
        byte first = this.source.read();
        if(first == 0x1E) {
            return new CFFNumber(this.readReal());
        } else {
            return new CFFNumber(this.readInteger(first));
        }
    }

    protected String getStringBySID(int sid) throws IOException {
        try {
            if (sid < CFFStandardStrings.N_STD_STRINGS) {
                return CFFStandardStrings.STANDARD_STRINGS[sid];
            } else {
                return new String(this.definedNames.get(sid -
                        CFFStandardStrings.N_STD_STRINGS));
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new IOException("Can't get string with given SID", e);
        }
    }

    /**
     * Instance of this class can represent int or double.
     */
    protected class CFFNumber {

        private int integer;
        private double real;
        boolean isInteger;

        CFFNumber(int integer) {
            this.integer = integer;
            this.isInteger = true;
        }

        CFFNumber(double real) {
            this.real = real;
            this.isInteger = false;
        }

        boolean isInteger() {
            return isInteger;
        }

        int getInteger() {
            return integer;
        }

        double getReal() {
            return real;
        }
    }
}
