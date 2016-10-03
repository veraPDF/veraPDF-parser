package org.verapdf.pd.font.truetype;

import org.apache.log4j.Logger;
import org.verapdf.as.ASAtom;
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
        if (this.isFontParsed) {
            return this.fontProgram;
        }
        this.isFontParsed = true;
        try {
            if (fontDescriptor.knownKey(ASAtom.FONT_FILE2)) {
                COSStream trueTypeFontFile =
                        getStreamFromObject(fontDescriptor.getKey(ASAtom.FONT_FILE2));
                this.fontProgram = new TrueTypeFontProgram(trueTypeFontFile.getData(
                        COSStream.FilterFlags.DECODE), this.isSymbolic(),
                        this.getEncoding());
                return this.fontProgram;
            } else if (fontDescriptor.knownKey(ASAtom.FONT_FILE3)) {
                COSStream trueTypeFontFile =
                        getStreamFromObject(fontDescriptor.getKey(ASAtom.FONT_FILE3));
                ASAtom subtype = trueTypeFontFile.getNameKey(ASAtom.SUBTYPE);
                if (subtype == ASAtom.OPEN_TYPE) {
                    this.fontProgram = new OpenTypeFontProgram(trueTypeFontFile.getData(
                            COSStream.FilterFlags.DECODE), false, this.isSymbolic(),
                            this.getEncoding());
                    return this.fontProgram;
                }
            }
        } catch (IOException e) {
            LOGGER.debug("Can't read TrueType font program.");
        }
        this.fontProgram = null;
        return null;
    }
}
