/*
 * This file is part of veraPDF Parser, a module of the veraPDF project.
 * Copyright (c) 2015-2026, veraPDF Consortium <info@verapdf.org>
 * All rights reserved.
 *
 * veraPDF Parser is free software: you can redistribute it and/or modify
 * it under the terms of either:
 *
 * The GNU General public license GPLv3+.
 * You should have received a copy of the GNU General public license along
 * with this program.  If not, see https://www.gnu.org/licenses/gpl-3.0.en.html.
 *
 * The Mozilla Public License MPLv2+.
 * You should have received a copy of the MPL license along with this program.
 */
package org.verapdf.pd.font.type1;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.verapdf.as.io.ASMemoryInStream;
import org.verapdf.io.SeekableInputStream;

import java.io.IOException;

class Type1CharStringParserTest {

    @Test
    void resolvesWidthThroughNestedSubrsRegardlessOfSubrOrder() throws IOException {
        byte[] firstSubr = encrypt(charstring(number(2245), operator(10), operator(11)));
        byte[] secondSubr = encrypt(charstring(number(0), number(239), operator(13), operator(11)));
        byte[] sourceBytes = new byte[1 + firstSubr.length + secondSubr.length];
        System.arraycopy(firstSubr, 0, sourceBytes, 1, firstSubr.length);
        System.arraycopy(secondSubr, 0, sourceBytes, 1 + firstSubr.length, secondSubr.length);
        try (SeekableInputStream source = new ASMemoryInStream(sourceBytes)) {
            Type1Subroutines subroutines = new Type1Subroutines(source);
            subroutines.addSubroutine(2240, 1, firstSubr.length, 0);
            subroutines.addSubroutine(2245, 1 + firstSubr.length, secondSubr.length, 0);

            Type1CharStringParser parser = new Type1CharStringParser(
                    new ASMemoryInStream(charstring(number(2240), operator(10), operator(14))),
                    subroutines);

            Assertions.assertEquals(239, parser.getWidth().getInteger());
            Type1CharStringParser cachedParser = new Type1CharStringParser(
                    new ASMemoryInStream(charstring(number(2240), operator(10), operator(14))),
                    subroutines);
            Assertions.assertEquals(239, cachedParser.getWidth().getInteger());
        }
    }

    private static byte[] charstring(byte[]... parts) {
        int length = 0;
        for (byte[] part : parts) {
            length += part.length;
        }
        byte[] result = new byte[length];
        int offset = 0;
        for (byte[] part : parts) {
            System.arraycopy(part, 0, result, offset, part.length);
            offset += part.length;
        }
        return result;
    }

    private static byte[] number(int value) {
        if (value >= -107 && value <= 107) {
            return new byte[]{(byte) (value + 139)};
        }
        return new byte[]{
                (byte) 255, (byte) (value >>> 24), (byte) (value >>> 16),
                (byte) (value >>> 8), (byte) value
        };
    }

    private static byte[] operator(int value) {
        return new byte[]{(byte) value};
    }

    private static byte[] encrypt(byte[] cleartext) {
        byte[] ciphertext = new byte[cleartext.length];
        int r = 4330;
        for (int i = 0; i < cleartext.length; i++) {
            int encoded = (cleartext[i] & 0xFF) ^ (r >> 8);
            ciphertext[i] = (byte) encoded;
            r = (encoded + r) * 52845 + 22719 & 0xFFFF;
        }
        return ciphertext;
    }
}
