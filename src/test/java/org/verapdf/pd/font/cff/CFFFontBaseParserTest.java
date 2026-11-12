/*
 * This file is part of veraPDF Parser, a module of the veraPDF project.
 * Copyright (c) 2015-2026, veraPDF Consortium <info@verapdf.org>
 * All rights reserved.
 *
 * veraPDF Parser is free software: you can redistribute it and/or modify
 * it under the terms of either:
 *
 * The GNU General public license GPLv3+.
 * You should have received a copy of the GNU General public license
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

public class CFFFontBaseParserTest {

    @Test
    public void testPrivateDictWidthsPreserveFractionalValues() throws IOException {
        byte[] privateDict = {
                0x1E, 0x12, (byte) 0xA5, (byte) 0xF0, 21,
                0x1E, 0x3A, 0x7F, 20
        };
        CFFFontBaseParser parser = new CFFFontBaseParser(
                SeekableInputStream.getSeekableStream(new ASMemoryInStream(privateDict)));

        parser.readPrivateDictUnit();
        parser.readPrivateDictUnit();
        parser.readPrivateDictUnit();
        parser.readPrivateDictUnit();

        assertEquals(12.5f, parser.nominalWidthX, 0.001f);
        assertEquals(3.7f, parser.defaultWidthX, 0.001f);
    }
}
