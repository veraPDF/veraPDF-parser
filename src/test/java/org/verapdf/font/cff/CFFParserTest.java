package org.verapdf.font.cff;

import org.junit.Test;
import org.verapdf.io.InternalInputStream;

import java.io.IOException;

/**
 * @author Sergey Shemyakov
 */
public class CFFParserTest {

    private String cMapPath = "src/test/resources/org/verapdf/font/cff/Times_Italic_adobe.cff";

    @Test
    public void testParser() throws IOException {
        CFFSubfontParserStarter parser =
                new CFFSubfontParserStarter(new InternalInputStream(cMapPath));
        parser.parse();
        System.out.println(""); //TODO: remove
    }
}
