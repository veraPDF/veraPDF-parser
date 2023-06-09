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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class InternalInputStreamTest {
    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void substreamOfStreamAtOffsetShouldReportCorrectOffset() throws IOException {
        byte[] buf = new byte[] { 0 };
        File file = temporaryFolder.newFile();
        Files.write(file.toPath(), new byte[] { 1, 2, 3 });

        try (InternalInputStream stream = InternalInputStream.createConcatenated(buf, Files.newInputStream(file.toPath()))) {
            assertEquals(0, stream.getOffset());
        }
    }


    @Test
    public void shouldSupportMarkAndReset() throws IOException {
        File file = temporaryFolder.newFile();
        Files.write(file.toPath(), new byte[] { 0, 1,2 });

        try (InternalInputStream stream = new InternalInputStream(file, true)) {

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
        File file = temporaryFolder.newFile();
        Files.write(file.toPath(), new byte[] { 1, 2, 3 });
        try (InternalInputStream stream = new InternalInputStream(file, true)) {
            stream.unread();
        }
    }

    @Test
    public void shouldBeAbleToUnread() throws IOException {
        File file = temporaryFolder.newFile();
        Files.write(file.toPath(), new byte[] { 0, 1, 2 });
        try (InternalInputStream stream = new InternalInputStream(file, true)) {
            assertEquals(0, stream.read());
            assertEquals(1, stream.read());
            stream.unread();
            assertEquals(1, stream.read());
        }
    }

    @Test
    public void shouldDelteTempFileWhenOnlyStreamIsClosed() throws IOException {
        File temp = temporaryFolder.newFile("test");
        InternalInputStream stream = new InternalInputStream(temp, true);
        stream.close();
        assertFalse(temp.exists());
    }

    @Test
    public void shouldDeleteTempFileWhenLastStreamIsClosed() throws IOException {
        File temp = temporaryFolder.newFile("test");
        InternalInputStream original = new InternalInputStream(temp, true);
        InternalInputStream copy = (InternalInputStream) original.getStream(0, original.getStreamLength());

        original.close();
        assertTrue(temp.exists());

        original.close();
        assertTrue(temp.exists());

        copy.close();
        assertFalse(temp.exists());
    }

    @Test
    public void shouldHaveIndependentPositions() throws IOException {
        File temp = temporaryFolder.newFile("test");
        Files.write(temp.toPath(), "abc".getBytes(StandardCharsets.US_ASCII));

        try (
            InternalInputStream original = new InternalInputStream(temp, true);
            InternalInputStream copy = (InternalInputStream) original.getStream(0, original.getStreamLength());
        ) {
            byte firstInOriginal = original.readByte();
            byte secondInOriginal = original.readByte();
            byte firstInCopy = copy.readByte();

            assertEquals('a', firstInOriginal);
            assertEquals('b', secondInOriginal);
            assertEquals('a', firstInCopy);
        }
    }

    @Test
    public void shouldHaveCorrectPositionWhenNested() throws IOException {
        File temp = temporaryFolder.newFile("test");
        Files.write(temp.toPath(), "abc".getBytes(StandardCharsets.US_ASCII));

        try (
            InternalInputStream a = new InternalInputStream(temp, true);
        ) {
            byte ba = a.readByte();
            assertEquals('a', ba);
            assertEquals(1, a.getOffset());
            try (InternalInputStream b = (InternalInputStream) a.getStream(1, a.getStreamLength())) {
                byte bb = b.readByte();
                assertEquals('b', bb);
                assertEquals(1, b.getOffset());
            }
        }
    }
    @Test
    public void shouldHaveCorrectPositionWhenDoublyNested() throws IOException {
        File temp = temporaryFolder.newFile("test");
        Files.write(temp.toPath(), "abc".getBytes(StandardCharsets.US_ASCII));

        try (
            InternalInputStream a = new InternalInputStream(temp, true);
        ) {
            assertEquals(0, a.getOffset());
            assertEquals(3, a.getStreamLength());
            byte ba = a.readByte();
            assertEquals('a', ba);
            assertEquals(1, a.getOffset());
            assertFalse(a.isEOF());
            try (InternalInputStream b = (InternalInputStream) a.getStream(1, a.getStreamLength())) {
                assertEquals(0, b.getOffset());
                assertEquals(2, b.getStreamLength());
                byte bb = b.readByte();
                assertEquals('b', bb);
                assertEquals(1, b.getOffset());
                assertFalse(b.isEOF());
                try (InternalInputStream c = (InternalInputStream) b.getStream(1, b.getStreamLength())) {
                    assertEquals(0, c.getOffset());
                    assertEquals(1, c.getStreamLength());
                    byte bc = c.readByte();
                    assertEquals('c', bc);
                    assertEquals(1, c.getOffset());
                    assertTrue(c.isEOF());
                }
            }
        }
    }

    @Test
    public void shouldBeAbleToReadWhenDoublyNested() throws IOException {
        File temp = temporaryFolder.newFile("test");
        byte[] buf = new byte[10 + 4];
        byte[] deadbeef = new byte[] { (byte) 0xde, (byte) 0xad, (byte) 0xbe, (byte) 0xef };
        for (int i = 0; i < 4; i++) {
            buf[i] = deadbeef[i % deadbeef.length];
        }
        for (int i = 0; i < buf.length - 4; i++) {
            buf[i + 4] = (byte) i;
        }
        Files.write(temp.toPath(), buf);

        try (InternalInputStream inner = new InternalInputStream(temp, true)) {
            assertEquals(0xde, inner.read());
            assertEquals(1, inner.getOffset());
            assertEquals(0xad, inner.read());
            assertEquals(2, inner.getOffset());
            try (InternalInputStream outer =
                (InternalInputStream) inner.getStream(2 + inner.getOffset(), inner.getStreamLength() - 4)) {

                assertEquals(10, outer.getStreamLength());
                for (int i = 0; i < 10; i++) {
                    assertEquals(i, outer.getOffset());
                    assertEquals(i, outer.readByte() & 0xff);
                }
                assertTrue(outer.isEOF());
            }
        }
    }

    @Test
    public void creatingSeekableStreamFromSeekableShouldConsumeIntermediary() throws IOException {
        Path temp = Files.createTempFile("test", "bin");
        temp.toFile().deleteOnExit();
        byte[] buf = new byte[10 + 4];
        byte[] deadbeef = new byte[] { (byte) 0xde, (byte) 0xad, (byte) 0xbe, (byte) 0xef };
        for (int i = 0; i < 4; i++) {
            buf[i] = deadbeef[i % deadbeef.length];
        }
        for (int i = 0; i < buf.length - 4; i++) {
            buf[i + 4] = (byte) i;
        }
        Files.write(temp, buf);
        try (InternalInputStream input = new InternalInputStream(temp.toFile(), true)) {
            assertEquals(4, input.read(buf, 4));
            try (
                SeekableInputStream seekable1 = SeekableInputStream.getSeekableStream(input);
            ) {
                assertEquals(0, seekable1.getOffset());
                assertEquals(10, seekable1.getStreamLength());
                assertEquals(0, seekable1.peek());
                try (SeekableInputStream seekable2 = SeekableInputStream.getSeekableStream(seekable1)) {
                    assertEquals(10, seekable1.getOffset());
                    assertEquals(-1, seekable1.read());

                    assertEquals(0, seekable2.getOffset());
                    assertEquals(10, seekable2.getStreamLength());
                    assertEquals(0, seekable2.read());
                }
            }
        }
    }
}
