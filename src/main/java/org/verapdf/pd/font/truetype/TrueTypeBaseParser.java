/**
 * This file is part of veraPDF Parser, a module of the veraPDF project.
 * Copyright (c) 2015, veraPDF Consortium <info@verapdf.org>
 * All rights reserved.
 *
 * veraPDF Parser is free software: you can redistribute it and/or modify
 * it under the terms of either:
 *
 * The GNU General public license GPLv3+.
 * You should have received a copy of the GNU General Public License
 * along with veraPDF Parser as the LICENSE.GPL file in the root of the source
 * tree.  If not, see http://www.gnu.org/licenses/ or
 * https://www.gnu.org/licenses/gpl-3.0.en.html.
 *
 * The Mozilla Public License MPLv2+.
 * You should have received a copy of the Mozilla Public License along with
 * veraPDF Parser as the LICENSE.MPL file in the root of the source tree.
 * If a copy of the MPL was not distributed with this file, you can obtain one at
 * http://mozilla.org/MPL/2.0/.
 */
package org.verapdf.pd.font.truetype;

import org.verapdf.as.io.ASInputStream;
import org.verapdf.io.SeekableInputStream;

import java.io.IOException;

/**
 * This class does low-level parsing of True Type Font.
 *
 * @author Sergey Shemyakov
 */
class TrueTypeBaseParser {

    private static final int TWO_POWER_16 = 65536;

    protected SeekableInputStream source;

    protected TrueTypeBaseParser(ASInputStream stream) throws IOException {
        this.source = SeekableInputStream.getSeekableStream(stream);
    }

    protected TrueTypeBaseParser(SeekableInputStream source) {
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
