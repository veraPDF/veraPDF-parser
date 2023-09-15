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
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.verapdf.as.io.ASInputStream;
import org.verapdf.as.io.ASMemoryInStream;
import org.verapdf.exceptions.VeraPDFParserException;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

/**
 * Tests behaviour of SeekableInputStreams, build from different kinds of source streams.
 *
 * @author Magnus Reftel
 */
@RunWith(Parameterized.class)
public class SeekableInputStreamParametricTest {
    public static final int SMALL_INPUT_SIZE = 10;
    public static final int LARGE_INPUT_SIZE = 15000;
    private final int size;
    private final InputStream input;

    public SeekableInputStreamParametricTest(String unused, int size, StreamSupplier input) throws IOException {
        this.size = size;
        this.input = input.get();
    }

    @Parameters(name = "{0}")
    public static List<Object[]> inputs() throws IOException {
        return Arrays.asList(
            new Object[] {
                "small, memory", SMALL_INPUT_SIZE,
                (StreamSupplier) () -> new ASMemoryInStream(buildBuffer(SMALL_INPUT_SIZE))
            },
            new Object[] {
                "large, memory", LARGE_INPUT_SIZE,
                (StreamSupplier) () -> new ASMemoryInStream(buildBuffer(LARGE_INPUT_SIZE))
            },
            new Object[] {
                "small, internal", SMALL_INPUT_SIZE,
                (StreamSupplier) () -> new InternalInputStream(buildFile(SMALL_INPUT_SIZE), true)
            },
            new Object[] {
                "large, internal", LARGE_INPUT_SIZE,
                (StreamSupplier) () -> new InternalInputStream(buildFile(LARGE_INPUT_SIZE), true)
            },
            new Object[] {
                "small, memory, at offset", SMALL_INPUT_SIZE,
                (StreamSupplier) () -> withOffset(4, new ASMemoryInStream(buildOffsetBuffer(4, SMALL_INPUT_SIZE)))
            },
            new Object[] {
                "large, memory, at offset", LARGE_INPUT_SIZE,
                (StreamSupplier) () -> withOffset(4, new ASMemoryInStream(buildOffsetBuffer(4, LARGE_INPUT_SIZE)))
            },
            new Object[] {
                "small, internal, at offset", SMALL_INPUT_SIZE,
                (StreamSupplier) () -> withOffset(4, new InternalInputStream(buildOffsetFile(4, SMALL_INPUT_SIZE), true))
            },
            new Object[] {
                "large, internal, at offset", LARGE_INPUT_SIZE,
                (StreamSupplier) () -> withOffset(4, new InternalInputStream(buildOffsetFile(4, LARGE_INPUT_SIZE), true))
            },
            new Object[] {
                "small, memory, nested", SMALL_INPUT_SIZE,
                (StreamSupplier) () -> nestedAtOffset(4, withOffset(2, new ASMemoryInStream(buildOffsetBuffer(4, SMALL_INPUT_SIZE))))
            },
            new Object[] {
                "large, memory, nested", LARGE_INPUT_SIZE,
                (StreamSupplier) () -> nestedAtOffset(4, withOffset(2, new ASMemoryInStream(buildOffsetBuffer(4, LARGE_INPUT_SIZE))))
            },
            new Object[] {
                "small, internal, nested", SMALL_INPUT_SIZE,
                (StreamSupplier) () -> nestedAtOffset(4, withOffset(2, new InternalInputStream(buildOffsetFile(4, SMALL_INPUT_SIZE), true)))
            },
            new Object[] {
                "large, internal, nested", LARGE_INPUT_SIZE,
                (StreamSupplier) () -> nestedAtOffset(4, withOffset(2, new InternalInputStream(buildOffsetFile(4, LARGE_INPUT_SIZE), true)))
            },
            new Object[] {
                "small, other", SMALL_INPUT_SIZE,
                (StreamSupplier) () -> new ByteArrayInputStream(buildBuffer(SMALL_INPUT_SIZE))
            },
            new Object[] {
                "large, other", LARGE_INPUT_SIZE,
                (StreamSupplier) () -> new ByteArrayInputStream(buildBuffer(LARGE_INPUT_SIZE))
            }
        );
    }

    private static <T extends InputStream> T withOffset(int offset, T in) throws IOException {
        byte[] buf = new byte[offset];
        assertEquals(offset, in.read(buf));
        return in;
    }

    private static ASInputStream nestedAtOffset(int offset, SeekableInputStream in) throws IOException {
        return in.getStream(offset, in.getStreamLength() - offset);
    }

    interface StreamSupplier {
        InputStream get() throws IOException;
    }

    private static byte[] buildBuffer(int size) {
        byte[] buf = new byte[size];
        for (int i = 0; i < buf.length; i++) {
            buf[i] = (byte) i;
        }
        return buf;
    }

    private static byte[] buildOffsetBuffer(int offset, int size) {
        byte[] buf = new byte[size + offset];
        byte[] deadbeef = new byte[] {(byte) 0xde, (byte) 0xad, (byte) 0xbe, (byte) 0xef};
        for (int i = 0; i < offset; i++) {
            buf[i] = deadbeef[offset % deadbeef.length];
        }
        for (int i = 0; i < buf.length - offset; i++) {
            buf[i + offset] = (byte) i;
        }
        return buf;
    }

    private static File buildFile(int size) throws IOException {
        Path path = Files.createTempFile("test", "bin");
        path.toFile().deleteOnExit();
        try (OutputStream out = Files.newOutputStream(path)) {
            for (int i = 0; i < size; i++) {
                out.write((byte) i);
            }
        }
        return path.toFile();
    }

    private static File buildOffsetFile(int offset, int size) throws IOException {
        Path path = Files.createTempFile("test", "bin");
        path.toFile().deleteOnExit();
        try (OutputStream out = Files.newOutputStream(path)) {
            byte[] deadbeef = new byte[] {(byte) 0xde, (byte) 0xad, (byte) 0xbe, (byte) 0xef};
            for (int i = 0; i < offset; i++) {
                out.write(deadbeef[i % deadbeef.length]);
            }
            for (int i = 0; i < size; i++) {
                out.write((byte) i);
            }
        }
        return path.toFile();
    }

    @Test
    public void seekableStreamShouldHaveSameContentsAsOriginal() throws IOException {
        input.mark(Integer.MAX_VALUE);
        try (
            SeekableInputStream seekable = SeekableInputStream.getSeekableStream(input);
        ) {
            input.reset();
            while (true) {
                int a = input.read();
                int b = seekable.read();
                assertEquals(a, b);
                if (a == -1) {
                    break;
                }
            }
        }
    }

    @Test
    public void closingTheOriginalStreamDoesNotCloseASeekableStreamBasedOnIt() throws IOException {
        try (
            SeekableInputStream seekable = SeekableInputStream.getSeekableStream(input);
        ) {
            input.close();
            assertEquals(0, seekable.read());
            assertEquals(1, seekable.read());
        }
    }

    @Test
    public void closingASeekableStreamDoesNotCloseTheStreamItIsBasedOn() throws IOException {
        input.mark(Integer.MAX_VALUE);
        try (
            SeekableInputStream seekable = SeekableInputStream.getSeekableStream(input);
        ) {
            input.reset();
            seekable.close();
            assertEquals(0, input.read());
        }
    }

    @Test
    public void creatingSeekableStreamShouldConsumeOriginal() throws IOException {
        try (
            SeekableInputStream seekable = SeekableInputStream.getSeekableStream(input);
        ) {
            assertEquals(-1, input.read());
            assertEquals(0, seekable.read());
        }
    }

    @Test
    public void creatingSeekableStreamFromSeekableShouldConsumeIntermediary() throws IOException {
        try (
            SeekableInputStream seekable1 = SeekableInputStream.getSeekableStream(input);
            SeekableInputStream seekable2 = SeekableInputStream.getSeekableStream(seekable1);
        ) {
            assertEquals(-1, seekable1.read());
            assertEquals(0, seekable2.read());
        }
    }

    @Test
    public void seekableStreamsShouldBePositionedAtTheCurrentPositionOfTheSource() throws IOException {
        for (int i = 0; i < 5; i++) {
            input.read();
        }
        try (
            SeekableInputStream seekable = SeekableInputStream.getSeekableStream(input);
        ) {
            assertEquals(5, seekable.read());
            assertEquals(6, seekable.read());
        }
    }

    @Test
    public void shouldReportCorrectLengthWhenCreatedFromStreamAtStart() throws IOException {
        try (
            SeekableInputStream seekable = SeekableInputStream.getSeekableStream(input);
        ) {
            assertEquals(size, seekable.getStreamLength());
        }
    }

    @Test
    public void shouldReportCorrectLengthWhenCreatedFromStreamAtOffset() throws IOException {
        input.read();
        try (
            SeekableInputStream seekable = SeekableInputStream.getSeekableStream(input);
        ) {
            assertEquals(size - 1, seekable.getStreamLength());
        }
    }

    @Test
    public void shouldReportCorrectPositionAfterSmallRead() throws IOException {
        try (
            SeekableInputStream seekable = SeekableInputStream.getSeekableStream(input);
        ) {
            byte buf[] = new byte[SMALL_INPUT_SIZE - 1];

            assertEquals(0, seekable.getOffset());

            int read = seekable.read();
            assertEquals(0, read);
            assertEquals(1, seekable.getOffset());

            int numRead = seekable.read(buf);
            assertEquals(buf.length, numRead);
            assertEquals(buf.length + 1, seekable.getOffset());
        }
    }

    @Test
    public void shouldReportCorrectPositionAfterLargeRead() throws IOException {
        if (size < LARGE_INPUT_SIZE) {  // Skip this test for small inputs
            return;
        }
        try (
            SeekableInputStream seekable = SeekableInputStream.getSeekableStream(input);
        ) {
            byte buf[] = new byte[LARGE_INPUT_SIZE - 1];

            assertEquals(0, seekable.getOffset());

            int read = seekable.read();
            assertEquals(0, read);
            assertEquals(1, seekable.getOffset());

            int numRead = seekable.read(buf);
            if (numRead >= buf.length) {
                assertEquals(buf.length, numRead);
                assertEquals(buf.length + 1, seekable.getOffset());
            }
        }
    }

    @Test
    public void shouldReportCorrectPositionAfterAbsoluteSeek() throws IOException {
        try (
            SeekableInputStream seekable = SeekableInputStream.getSeekableStream(input);
        ) {
            assertEquals(0, seekable.getOffset());

            seekable.seek(5);
            assertEquals(5, seekable.getOffset());
        }
    }

    @Test
    public void shouldReportCorrectPositionAfterAbsoluteSeekWhenInputIsAtAnOffset() throws IOException {
        input.read();
        try (
            SeekableInputStream seekable = SeekableInputStream.getSeekableStream(input);
        ) {
            assertEquals(0, seekable.getOffset());

            seekable.seek(5);
            assertEquals(5, seekable.getOffset());
        }
    }

    @Test
    public void shouldReportCorrectPositionAfterRelativeSeek() throws IOException {
        try (
            SeekableInputStream seekable = SeekableInputStream.getSeekableStream(input);
        ) {
            assertEquals(0, seekable.getOffset());

            seekable.seekFromCurrentPosition(5);
            assertEquals(5, seekable.getOffset());
        }
    }

    @Test
    public void shouldReportCorrectPositionAfterRelativeSeekWhenInputIsAtAnOffset() throws IOException {
        input.read();
        try (
            SeekableInputStream seekable = SeekableInputStream.getSeekableStream(input);
        ) {
            assertEquals(0, seekable.getOffset());

            seekable.seekFromCurrentPosition(5);
            assertEquals(5, seekable.getOffset());
        }
    }

    @Test
    public void shouldReportCorrectPositionAfterSeekFromEnd() throws IOException {
        try (
            SeekableInputStream seekable = SeekableInputStream.getSeekableStream(input);
        ) {
            assertEquals(0, seekable.getOffset());
            seekable.seekFromEnd(1);
            assertEquals(size - 1, seekable.getOffset());
        }
    }

    @Test
    public void shouldReportCorrectPositionAfterSeekFromEndWhenInputIsAtAnOffset() throws IOException {
        input.read();
        try (
            SeekableInputStream seekable = SeekableInputStream.getSeekableStream(input);
        ) {
            assertEquals(0, seekable.getOffset());
            seekable.seekFromEnd(1);
            assertEquals(size - 2, seekable.getOffset());
        }
    }

    @Test
    public void closingASeekableStreamShouldNotInterfereWithTheOriginal() throws IOException {
        assertEquals(0, input.read());
        input.mark(Integer.MAX_VALUE);
        assertEquals(1, input.read());
        try (
            SeekableInputStream seekable = SeekableInputStream.getSeekableStream(input);
        ) {
            assertEquals(2, seekable.read());
        }
        input.reset();
        assertEquals(1, input.read());
    }

    @Test(expected = IOException.class)
    public void shouldNotBeAbleToUnreadAtStart() throws IOException {
        try (
            SeekableInputStream seekable = SeekableInputStream.getSeekableStream(input);
        ) {
            seekable.unread();
        }
    }

    @Test
    public void peekShouldReturnCorrectData() throws IOException {
        try (
            SeekableInputStream seekable = SeekableInputStream.getSeekableStream(input);
        ) {
            for (int i = 0; i < size; i++) {
                int peeked = seekable.peek();
                int read = seekable.read();
                assertEquals(read, peeked);
                assertEquals(i + 1, seekable.getOffset());
            }
        }
    }

    @Test
    public void shouldNotThrowExceptionsAboutExceedingMaxSizeWhenNotExeedingMaxSize() throws IOException {
        try (
            SeekableInputStream seekable = SeekableInputStream.getSeekableStream(input, size);
        ) {
            assertEquals(seekable.getOffset(), 0);
        }
    }

    @Test
    public void shouldThrowExceptionsAboutExceedingMaxSizeWhenExeedingMaxSize() {
        assertThrows(VeraPDFParserException.class, () -> SeekableInputStream.getSeekableStream(input, size - 1));
    }
}
