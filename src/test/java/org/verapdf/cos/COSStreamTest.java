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
package org.verapdf.cos;

import org.junit.Test;
import org.verapdf.as.ASAtom;
import org.verapdf.as.io.ASInputStream;
import org.verapdf.as.io.ASMemoryInStream;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;

/**
 * @author Sergey Shemyakov
 */
public class COSStreamTest {

    private static final String SAMPLE_DATA = "Just some generic data";

    @Test
    public void test() throws IOException {
        byte[] asciiHexData = "4a75737420736f6d652067656e657269632064617461".getBytes(StandardCharsets.ISO_8859_1);    //"Just some generic data" in hex form
        ASInputStream asciiHexStream = new ASMemoryInStream(asciiHexData);
        COSObject cosStream = COSStream.construct(asciiHexStream);
        cosStream.setKey(ASAtom.FILTER, COSName.construct(ASAtom.ASCII_HEX_DECODE));
        ((COSStream) cosStream.get()).setFilters(new COSFilters(COSName.construct(ASAtom.FLATE_DECODE)));
        byte[] buf = new byte[100];
        int read = cosStream.getData(COSStream.FilterFlags.DECODE).read(buf, 100);
        String message = new String(Arrays.copyOf(buf, read), StandardCharsets.ISO_8859_1);
        assertEquals(message, SAMPLE_DATA);
    }

}
