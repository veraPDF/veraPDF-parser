package org.verapdf.pd.font.truetype;

import org.junit.Test;
import org.verapdf.as.ASAtom;
import org.verapdf.cos.COSName;
import org.verapdf.cos.COSObject;
import org.verapdf.io.InternalInputStream;

import java.io.IOException;

import static org.junit.Assert.assertTrue;

/**
 * @author Sergey Shemyakov
 */
public class TrueTypeParserTest {

    private static final String MONO_FONT_PATH = "src/test/resources/org/verapdf/font/truetype/SourceCodePro-Bold.ttf";
    private static final String REGULAR_FONT_PATH = "src/test/resources/org/verapdf/font/truetype/LiberationSans-Regular.ttf";
    private static final boolean IS_SYMBOLIC = false;
    private static final COSObject ENCODING = COSName.construct(ASAtom.MAC_ROMAN_ENCODING);

    @Test
    public void testMonospaced() throws IOException {

        TrueTypeFontProgram font = new TrueTypeFontProgram(new InternalInputStream(MONO_FONT_PATH),
                IS_SYMBOLIC, ENCODING);
        font.parseFont();
        assertTrue(font.getWidth("z") == 600f);
        assertTrue(font.getWidth("yakute") == 1000f);
    }

    @Test
    public void testRegular() throws IOException {
        TrueTypeFontProgram font = new TrueTypeFontProgram(new InternalInputStream(REGULAR_FONT_PATH),
                IS_SYMBOLIC, ENCODING);
        font.parseFont();
        assertTrue((int) font.getWidth("z") == 500);
        assertTrue((int) font.getWidth("zero") == 556);
        assertTrue((int) font.getWidth("yakute") == 365);
    }
}
