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
