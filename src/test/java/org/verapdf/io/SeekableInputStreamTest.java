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
package org.verapdf.io;

import org.junit.Test;
import org.verapdf.as.io.ASMemoryInStream;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.junit.Assert.assertTrue;

/**
 * Tests correct creation of InternalInputStream and ASMemoryInStream.
 *
 * @author Sergey Shemyakov
 */
public class SeekableInputStreamTest {

    @Test
    public void test() throws IOException {
        byte[] one = new byte[10];
        byte[] two = new byte[10239];
        byte[] three = new byte[15000];
        InputStream streamOne = new ByteArrayInputStream(one);
        InputStream streamTwo = new ByteArrayInputStream(two);
        InputStream streamThree = new ByteArrayInputStream(three);
        SeekableInputStream ssOne = SeekableInputStream.getSeekableStream(streamOne);
        SeekableInputStream ssTwo = SeekableInputStream.getSeekableStream(streamTwo);
        SeekableInputStream ssThree = SeekableInputStream.getSeekableStream(streamThree);
        assertTrue(ssOne instanceof ASMemoryInStream);
        assertTrue(ssTwo instanceof ASMemoryInStream);
        assertTrue(ssThree instanceof InternalInputStream);
    }

}
