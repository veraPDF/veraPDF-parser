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
public class SeekableStreamTest {

    @Test
    public void test() throws IOException {
        byte[] one = new byte[10];
        byte[] two = new byte[10239];
        byte[] three = new byte[15000];
        InputStream streamOne = new ByteArrayInputStream(one);
        InputStream streamTwo = new ByteArrayInputStream(two);
        InputStream streamThree = new ByteArrayInputStream(three);
        SeekableStream ssOne = SeekableStream.getSeekableStream(streamOne);
        SeekableStream ssTwo = SeekableStream.getSeekableStream(streamTwo);
        SeekableStream ssThree = SeekableStream.getSeekableStream(streamThree);
        assertTrue(ssOne instanceof ASMemoryInStream);
        assertTrue(ssTwo instanceof ASMemoryInStream);
        assertTrue(ssThree instanceof InternalInputStream);
    }

}
