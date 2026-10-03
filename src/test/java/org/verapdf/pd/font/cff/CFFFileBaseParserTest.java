/*
 * This file is part of veraPDF Parser, a module of the veraPDF project.
 * Copyright (c) 2015-2026, veraPDF Consortium <info@verapdf.org>
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

import org.junit.Test;
import org.verapdf.as.io.ASMemoryInStream;
import org.verapdf.io.SeekableInputStream;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class CFFFileBaseParserTest {

    private static CFFFileBaseParser parser(int... bytes) throws IOException {
        byte[] data = new byte[bytes.length];
        for (int i = 0; i < bytes.length; ++i) {
            data[i] = (byte) bytes[i];
        }
        return new CFFFileBaseParser(SeekableInputStream.getSeekableStream(new ASMemoryInStream(data)));
    }

    @Test(expected = IOException.class)
    public void testReadIndexWithOversizedLastOffset() throws IOException {
        parser(0x00, 0x01, 0x04, 0x00, 0x00, 0x00, 0x01, 0x3F, 0xFF, 0xFF, 0xFF).readIndex();
    }

    @Test(expected = IOException.class)
    public void testReadIndexWithNegativeLastOffset() throws IOException {
        parser(0x00, 0x01, 0x04, 0x00, 0x00, 0x00, 0x01, 0xFF, 0xFF, 0xFF, 0xFF).readIndex();
    }

    @Test(expected = IOException.class)
    public void testReadIndexWithOversizedOffSize() throws IOException {
        parser(0x00, 0x01, 0x05, 0x00, 0x00, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00, 0x00, 0x04).readIndex();
    }

    @Test
    public void testReadValidIndex() throws IOException {
        CFFIndex index = parser(0x00, 0x01, 0x01, 0x01, 0x04, 'a', 'b', 'c').readIndex();
        assertEquals(1, index.size());
        assertEquals(3, index.getDataLength());
    }
}
