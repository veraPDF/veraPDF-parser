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
package org.verapdf.cos.filters;

import org.junit.Test;
import org.verapdf.cos.COSDictionary;
import org.verapdf.io.SeekableInputStream;

import java.io.FileInputStream;
import java.io.IOException;

import static org.junit.Assert.assertEquals;

/**
 * @author Sergey Shemyakov
 */
public class COSFilterLZWDecodeTest {

    private String lzwPath = "src/test/resources/org/verapdf/cos/filters/lzw";

    @Test
    public void test() throws IOException {
        FileInputStream stream = new FileInputStream(lzwPath);
        COSFilterLZWDecode lzwDecode = new COSFilterLZWDecode(SeekableInputStream.getSeekableStream(stream), (COSDictionary) COSDictionary.construct().get());
        byte[] buf = new byte[2048];
        int read = lzwDecode.read(buf, 2048);
        assertEquals(buf[0], 66);
        assertEquals(read, 102);
        assertEquals(buf[50], 46);
    }

}
