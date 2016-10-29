package org.verapdf.cos.filters;

import org.junit.Test;
import org.verapdf.io.SeekableStream;

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
        COSFilterLZWDecode lzwDecode = new COSFilterLZWDecode(SeekableStream.getSeekableStream(stream));
        byte[] buf = new byte[2048];
        int read = lzwDecode.read(buf, 2048);
        assertEquals(buf[0], 66);
        assertEquals(read, 102);
        assertEquals(buf[50], 46);
    }

}
