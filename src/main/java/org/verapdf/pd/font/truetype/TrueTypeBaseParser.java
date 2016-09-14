package org.verapdf.pd.font.truetype;

import org.verapdf.as.io.ASInputStream;
import org.verapdf.io.InternalInputStream;

import java.io.IOException;

/**
 * This class does low-level parsing of True Type Font.
 *
 * @author Sergey Shemyakov
 */
class TrueTypeBaseParser {

    private static final int TWO_POWER_16 = 65536;

    protected InternalInputStream source;

    protected TrueTypeBaseParser(ASInputStream stream) throws IOException {
        this.source = new InternalInputStream(stream);
    }

    protected TrueTypeBaseParser(InternalInputStream source) {
        this.source = source;
    }

    /**
     * Empty constructor for inherited classes.
     */
    protected TrueTypeBaseParser() {}

    protected int readByte() throws IOException {
        return this.source.readByte() & 0xFF;
    }

    protected byte readChar() throws IOException {
        return this.source.readByte();
    }

    protected int readUShort() throws IOException {
        int highOrder = (this.source.readByte() & 0xFF) << 8;
        return highOrder | (this.source.readByte() & 0xFF);
    }

    protected int readShort() throws IOException {
        return (short) readUShort();
    }

    protected long readULong() throws IOException {
        long res = readUShort();
        res = res << 16;
        return res | readUShort();
    }

    protected int readLong() throws IOException {
        int res = readUShort();
        res = res << 16;
        return res | readUShort();
    }

    protected float readFixed() throws IOException {
        byte[] buf = new byte[4];
        this.source.read(buf, 4);
        int integer = 0;
        for (int i = 0; i < 3; ++i) {
            integer |= (buf[i] & 0xFF);
            integer <<= 8;
        }
        integer |= buf[3] & 0xFF;
        float res = integer;
        return res / TWO_POWER_16;
    }

    protected int readFWord() throws IOException {
        return this.readShort();
    }

    protected int readUFWord() throws IOException {
        return this.readUShort();
    }
}
