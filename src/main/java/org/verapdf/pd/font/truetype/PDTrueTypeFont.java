package org.verapdf.pd.font.truetype;

import org.apache.log4j.Logger;
import org.verapdf.as.ASAtom;
import org.verapdf.as.io.ASInputStream;
import org.verapdf.cos.COSDictionary;
import org.verapdf.cos.COSStream;
import org.verapdf.pd.font.FontProgram;
import org.verapdf.pd.font.PDSimpleFont;
import org.verapdf.pd.font.opentype.OpenTypeFontProgram;

import java.io.IOException;

/**
 * @author Sergey Shemyakov
 */
public class PDTrueTypeFont extends PDSimpleFont {

    private static final Logger LOGGER = Logger.getLogger(PDTrueTypeFont.class);

    public PDTrueTypeFont(COSDictionary dictionary) {
        super(dictionary);
    }

    @Override
    public FontProgram getFontProgram() {
        if (fontDescriptor.knownKey(ASAtom.FONT_FILE2)) {
            COSStream trueTypeFontFile =
                    (COSStream) fontDescriptor.getKey(ASAtom.FONT_FILE2).get();
            try {
                return new TrueTypeFontProgram(trueTypeFontFile.getData(
                        COSStream.FilterFlags.DECODE), this.isSymbolic(),
                        this.getEncoding());
            } catch (IOException e) {
                LOGGER.error("Can't read TrueType font program.");
            }
        } else if (fontDescriptor.knownKey(ASAtom.FONT_FILE3)) {
            COSStream trueTypeFontFile =
                    (COSStream) fontDescriptor.getKey(ASAtom.FONT_FILE3).get();
            ASAtom subtype = trueTypeFontFile.getNameKey(ASAtom.SUBTYPE);
            if (subtype == ASAtom.OPEN_TYPE) {
                return new OpenTypeFontProgram(trueTypeFontFile.getData(
                        COSStream.FilterFlags.DECODE), false, this.isSymbolic(),
                        this.getEncoding());
            }
        }
        return null;
    }

    @Override
    public int readCode(ASInputStream stream) throws IOException {
        return stream.read();
    }
}
