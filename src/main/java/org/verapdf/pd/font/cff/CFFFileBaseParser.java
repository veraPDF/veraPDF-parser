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
package org.verapdf.pd.font.cff;

import org.verapdf.as.io.ASInputStream;
import org.verapdf.io.SeekableInputStream;
import org.verapdf.pd.font.CFFNumber;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * This class does low-level parsing of CFF file.
 *
 * @author Sergey Shemyakov
 */
class CFFFileBaseParser {

    protected SeekableInputStream source;
    protected CFFIndex definedNames;

    CFFFileBaseParser(ASInputStream source) throws IOException {
        this.source = SeekableInputStream.getSeekableStream(source);
    }

    CFFFileBaseParser(SeekableInputStream source) {
        this.source = source;
    }

    protected int readCard8() throws IOException {
        return this.source.readByte() & 0xFF;
    }

    protected int readCard16() throws IOException {
        int highOrder = (this.source.readByte() & 0xFF) << 8;
        return highOrder | (this.source.readByte() & 0xFF);
    }

    private long readOffset(int offSize) throws IOException {
        long res = 0;
        for (int i = 0; i < offSize - 1; ++i) {
            res |= (this.source.readByte() & 0xFF);
            res <<= 8;
        }
        res |= (this.source.readByte() & 0xFF);
        return res;
    }

    protected CFFIndex readIndex() throws IOException {
        int count = readCard16();
        if (count == 0) {
            return new CFFIndex(0, 0, new int[0], new byte[0]);
        }
        int offSize = readCard8();
        int[] offset = new int[count + 1];
        for (int i = 0; i < count + 1; ++i) {
            offset[i] = (int) readOffset(offSize);
        }
        if (offset[count] < 1) {
            throw new IOException("Wrong index data offset");
        }
        byte[] data = new byte[offset[count] - 1];
        if (source.read(data, data.length) != data.length) {
            throw new IOException("End of stream is reached");
        }
        int offsetShift = 3 + offSize * (count + 1);
        return new CFFIndex(count, offsetShift, offset, data);
    }

    protected void readHeader() throws IOException {
        readCard8();
        readCard8();
        int hdrSize = readCard8();
        this.source.seek(hdrSize);
    }

    private float readReal() throws IOException {
        StringBuilder builder = new StringBuilder();
        int buf;
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
                        default:    // Can not be reached
                            break parsing;
                    }
                }
            }
        }
        return Float.parseFloat(builder.toString());
    }

    private int readInteger(byte b) throws IOException {
        int firstByteValue = b & 0xFF;
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
        byte first = this.source.readByte();
        if (first == 0x1E) {
            return new CFFNumber(this.readReal());
        } else {
            return new CFFNumber(this.readInteger(first));
        }
    }

    protected String getStringBySID(int sid) throws IOException {
        try {
            if (sid < CFFPredefined.N_STD_STRINGS) {
                return CFFPredefined.STANDARD_STRINGS[sid];
            } else {
                return new String(this.definedNames.get(sid -
                        CFFPredefined.N_STD_STRINGS), StandardCharsets.ISO_8859_1);
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new IOException("Can't get string with given SID", e);
        }
    }
}
