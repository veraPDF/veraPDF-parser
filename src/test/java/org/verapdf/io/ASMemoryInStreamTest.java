/**
 * This file is part of veraPDF Parser, a module of the veraPDF project.
 * Copyright (c) 2023, veraPDF Consortium <info@verapdf.org>
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

import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class ASMemoryInStreamTest {

    @Test
    public void substreamOfStreamAtStartShouldReportCorrectOffset() throws IOException {
        try (ASMemoryInStream stream = new ASMemoryInStream(new byte[] { 0, 1, 2 })) {
            try (ASMemoryInStream copy = new ASMemoryInStream(stream, 0, 3)) {
                assertEquals(0, copy.getOffset());
            }
        }
    }

    @Test
    public void substreamOfStreamAtStartShouldReportCorrectLength() throws IOException {
        try (ASMemoryInStream stream = new ASMemoryInStream(new byte[] { 0, 1, 2 })) {
            try (ASMemoryInStream copy = new ASMemoryInStream(stream, 0, 3)) {
                assertEquals(3, copy.getStreamLength());
            }
        }
    }

    @Test
    public void substreamOfStreamAtOffsetShouldReportCorrectOffset() throws IOException {
        try (ASMemoryInStream stream = new ASMemoryInStream(new byte[] { 0, 1, 2 })) {
            try (ASMemoryInStream copy = new ASMemoryInStream(stream, 1, 2)) {
                assertEquals(0, copy.getOffset());
            }
        }
    }

    @Test
    public void substreamOfStreamAtOffsetShouldReportCorrectLength() throws IOException {
        try (ASMemoryInStream stream = new ASMemoryInStream(new byte[] { 0, 1, 2 })) {
            try (ASMemoryInStream copy = new ASMemoryInStream(stream, 1, 2)) {
                assertEquals(2, copy.getStreamLength());
            }
        }
    }

    @Test
    public void shouldSupportMarkAndReset() throws IOException {
        try (ASMemoryInStream stream = new ASMemoryInStream(new byte[] { 0, 1, 2 })) {

            assertEquals(0, stream.read());
            stream.mark(Integer.MAX_VALUE);

            assertEquals(1, stream.read());
            assertEquals(2, stream.read());

            stream.reset();
            assertEquals(1, stream.read());
        }
    }

    @Test(expected = IOException.class)
    public void shouldNotBeAbleToUnreadAtStart() throws IOException {
        try (ASMemoryInStream stream = new ASMemoryInStream(new byte[] { 0, 1, 2 })) {
            stream.unread();
        }
    }

    @Test
    public void shouldBeAbleToUnread() throws IOException {
        try (ASMemoryInStream stream = new ASMemoryInStream(new byte[] { 0, 1, 2 })) {
            assertEquals(0, stream.read());
            assertEquals(1, stream.read());
            stream.unread();
            assertEquals(1, stream.read());
        }
    }
}
