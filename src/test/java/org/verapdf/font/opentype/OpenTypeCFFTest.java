package org.verapdf.font.opentype;

import org.junit.Test;
import org.verapdf.as.io.ASInputStream;
import org.verapdf.font.openType.OpenTypeFont;
import org.verapdf.io.InternalInputStream;

import java.io.IOException;

/**
 * @author Sergey Shemyakov
 */
public class OpenTypeCFFTest {

    private String fontFilePath = "src/test/resources/org/verapdf/font/opentype/ShortStack-Regular.otf";

    @Test
    public void test() throws IOException {
        ASInputStream stream = new InternalInputStream(fontFilePath);
        OpenTypeFont font = new OpenTypeFont(stream, true, false, null);
        font.parseFont();
    }

}
