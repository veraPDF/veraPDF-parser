package org.verapdf.font.cff;

import org.junit.Test;
import org.verapdf.as.io.ASInputStream;
import org.verapdf.io.InternalInputStream;

import java.io.IOException;

import static org.junit.Assert.assertTrue;

/**
 * @author Sergey Shemyakov
 */
public class CFFTest {

    private String fontFilePath = "src/test/resources/org/verapdf/font/cff/Times_Italic_adobe.cff";

    @Test
    public void test() throws IOException {
        ASInputStream stream = new InternalInputStream(fontFilePath);
        CFFFont font = new CFFFont(stream);
        font.parseFont();
        assertTrue(font.getWidth(97) == 500.);
        assertTrue(font.getWidth(198) == 333.);
    }
}
