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
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

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

    @Test
    public void shouldBeAbleToReadAllBytes() throws IOException {
        byte buf[] = { 0, 1, 2 };
        try (ASMemoryInStream stream = new ASMemoryInStream(buf)) {
            for (int i = 0; i < buf.length; i++) {
                byte b = stream.readByte();
                assertEquals(i, b);
            }
            assertTrue(stream.isEOF());
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

    @Test
    public void shouldHaveCorrectPositionWhenNested() throws IOException {
        try (
            ASMemoryInStream a = new ASMemoryInStream("abc".getBytes(StandardCharsets.US_ASCII));
        ) {
            byte ba = a.readByte();
            assertEquals('a', ba);
            assertEquals(1, a.getOffset());
            try (ASMemoryInStream b = (ASMemoryInStream) a.getStream(1, a.getStreamLength() - 1)) {
                byte bb = b.readByte();
                assertEquals('b', bb);
                assertEquals(1, b.getOffset());
            }
        }
    }

    @Test
    public void shouldHaveCorrectPositionWhenDoublyNested() throws IOException {
        try (
            ASMemoryInStream a = new ASMemoryInStream("abc".getBytes(StandardCharsets.US_ASCII));
        ) {
            byte ba = a.readByte();
            assertEquals('a', ba);
            assertEquals(1, a.getOffset());
            assertEquals(3, a.getStreamLength());
            assertFalse(a.isEOF());
            try (ASMemoryInStream b = (ASMemoryInStream) a.getStream(1, a.getStreamLength() - 1)) {
                byte bb = b.readByte();
                assertEquals('b', bb);
                assertEquals(1, b.getOffset());
                assertEquals(2, b.getStreamLength());
                assertFalse(b.isEOF());
                try (ASMemoryInStream c = (ASMemoryInStream) b.getStream(1, b.getStreamLength() - 1)) {
                    byte bc = c.readByte();
                    assertEquals('c', bc);
                    assertEquals(1, c.getOffset());
                    assertEquals(1, c.getStreamLength());
                    assertTrue(c.isEOF());
                }
            }
        }
    }

    @Test
    public void shouldBeAbleToReadWhenDoublyNested() throws IOException {
        byte[] buf = new byte[10 + 4];
        byte[] deadbeef = new byte[] { (byte) 0xde, (byte) 0xad, (byte) 0xbe, (byte) 0xef };
        for (int i = 0; i < 4; i++) {
            buf[i] = deadbeef[i % deadbeef.length];
        }
        for (int i = 0; i < buf.length - 4; i++) {
            buf[i + 4] = (byte) i;
        }
        try (ASMemoryInStream inner = new ASMemoryInStream(buf)) {
            assertEquals(0xde, inner.read());
            assertEquals(1, inner.getOffset());
            assertEquals(0xad, inner.read());
            assertEquals(2, inner.getOffset());
            try (ASMemoryInStream outer =
                (ASMemoryInStream) inner.getStream(2 + inner.getOffset(), inner.getStreamLength() - 4)) {

                assertEquals(10, outer.getStreamLength());
                for (int i = 0; i < 10; i++) {
                    assertEquals(i, outer.getOffset());
                    assertEquals(i, outer.readByte() & 0xff);
                }
                assertTrue(outer.isEOF());
            }
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void sholdNotBeAbleToCreateSubstreamPastEnd() throws IOException {
        byte[] buf = new byte[2];
        try (ASMemoryInStream inner = new ASMemoryInStream(buf)) {
            try (ASMemoryInStream outer = (ASMemoryInStream) new ASMemoryInStream(inner, 1, 2)) {
            }
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void sholdNotBeAbleToCreateSubstreamBeforeStart() throws IOException {
        byte[] buf = new byte[2];
        try (ASMemoryInStream inner = new ASMemoryInStream(buf)) {
            try (ASMemoryInStream outer = (ASMemoryInStream) new ASMemoryInStream(inner, -1, 2)) {
            }
        }
    }

    @Test
    public void shouldBeAbleToCreateSubstreamBeforeStartOfOffsetSubstream() throws IOException {
        byte[] buf = new byte[] {1, 2, 3};
        try (ASMemoryInStream grandparent = new ASMemoryInStream(buf)) {
            try (ASMemoryInStream parent = (ASMemoryInStream) new ASMemoryInStream(grandparent, 1, 2)) {
                try (ASMemoryInStream child = (ASMemoryInStream) new ASMemoryInStream(parent, -1, 3)) {
                    assertEquals(1, child.readByte() & 0xff);
                }
            }
        }
    }
}
