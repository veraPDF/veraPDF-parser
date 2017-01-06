package org.verapdf.pd.font.truetype;

import org.verapdf.as.ASAtom;
import org.verapdf.as.io.ASInputStream;
import org.verapdf.cos.COSDictionary;
import org.verapdf.cos.COSStream;
import org.verapdf.pd.font.FontProgram;
import org.verapdf.pd.font.PDSimpleFont;
import org.verapdf.pd.font.opentype.OpenTypeFontProgram;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Sergey Shemyakov
 */
public class PDTrueTypeFont extends PDSimpleFont {

    private static final Logger LOGGER = Logger.getLogger(PDTrueTypeFont.class.getCanonicalName());

    public PDTrueTypeFont(COSDictionary dictionary) {
        super(dictionary);
    }

    @Override
    public FontProgram getFontProgram() {
        if (this.isFontParsed) {
            return this.fontProgram;
        }
        this.isFontParsed = true;
        if (fontDescriptor.canParseFontFile(ASAtom.FONT_FILE2)) {
            COSStream trueTypeFontFile = fontDescriptor.getFontFile2();
            try (ASInputStream fontData = trueTypeFontFile.getData(
                    COSStream.FilterFlags.DECODE)) {
                this.fontProgram = new TrueTypeFontProgram(fontData, this.isSymbolic(),
                        this.getEncoding());
                return this.fontProgram;
            } catch (IOException e) {
                LOGGER.log(Level.FINE, "Can't read TrueType font program.", e);
            }
        } else if (fontDescriptor.canParseFontFile(ASAtom.FONT_FILE3)) {
            COSStream trueTypeFontFile = fontDescriptor.getFontFile3();
            ASAtom subtype = trueTypeFontFile.getNameKey(ASAtom.SUBTYPE);
            if (subtype == ASAtom.OPEN_TYPE) {
                try (ASInputStream fontData = trueTypeFontFile.getData(
                        COSStream.FilterFlags.DECODE)) {
                    this.fontProgram = new OpenTypeFontProgram(fontData, false,
                            this.isSymbolic(), this.getEncoding(), null, this.isSubset());
                    return this.fontProgram;
                } catch (IOException e) {
                    LOGGER.log(Level.FINE, "Can't read TrueType font program.", e);
                }
            }
        }
        this.fontProgram = null;
        return null;
    }
}
