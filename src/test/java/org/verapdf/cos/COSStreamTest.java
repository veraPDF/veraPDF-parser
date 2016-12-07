package org.verapdf.cos;

import org.junit.Test;
import org.verapdf.as.ASAtom;
import org.verapdf.as.io.ASInputStream;
import org.verapdf.as.io.ASMemoryInStream;

import java.io.IOException;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;

/**
 * @author Sergey Shemyakov
 */
public class COSStreamTest {

    private static final String SAMPLE_DATA = "Just some generic data";

    @Test
    public void test() throws IOException {
        byte[] asciiHexData = "4a75737420736f6d652067656e657269632064617461".getBytes();    //"Just some generic data" in hex form
        ASInputStream asciiHexStream = new ASMemoryInStream(asciiHexData);
        COSObject cosStream = COSStream.construct(asciiHexStream);
        cosStream.setKey(ASAtom.FILTER, COSName.construct(ASAtom.ASCII_HEX_DECODE));
        ((COSStream) cosStream.get()).setFilters(new COSFilters(COSName.construct(ASAtom.FLATE_DECODE)));
        System.out.println();
        byte[] buf = new byte[100];
        int read = cosStream.getData(COSStream.FilterFlags.DECODE).read(buf, 100);
        String message = new String(Arrays.copyOf(buf, read));
        assertEquals(message, SAMPLE_DATA);
    }

}
