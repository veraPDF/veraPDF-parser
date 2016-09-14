package org.verapdf.pd.font;

import org.apache.log4j.Logger;
import org.verapdf.as.ASAtom;
import org.verapdf.cos.COSDictionary;
import org.verapdf.cos.COSName;
import org.verapdf.cos.COSObject;
import org.verapdf.cos.COSStream;
import org.verapdf.pd.font.cff.CFFFontProgram;
import org.verapdf.pd.font.opentype.OpenTypeFontProgram;
import org.verapdf.pd.font.truetype.TrueTypeFontProgram;

import java.io.IOException;

/**
 * @author Sergey Shemyakov
 */
public class PDCIDFont extends PDFont {

    private static final Logger LOGGER = Logger.getLogger(PDCIDFont.class);

    public PDCIDFont(COSDictionary dictionary) {
        super(dictionary);
    }

    public COSStream getCIDSet() {
        COSObject cidSet = this.fontDescriptor.getKey(ASAtom.CID_SET);
        return cidSet == null ? null : (COSStream) cidSet.get();
    }

    public COSObject getCIDToGIDMap() {
        return this.dictionary.getKey(ASAtom.CID_TO_GID_MAP);
    }

    @Override
    public FontProgram getFontProgram() {
        if (this.isFontParsed) {
            return this.fontProgram;
        }
        this.isFontParsed = true;

        if (fontDescriptor.knownKey(ASAtom.FONT_FILE2) &&
                this.getSubtype() == ASAtom.CID_FONT_TYPE2) {
            try {
                COSStream trueTypeFontFile =
                        getStreamFromObject(fontDescriptor.getKey(ASAtom.FONT_FILE2));
                this.fontProgram = new TrueTypeFontProgram(
                        trueTypeFontFile.getData(COSStream.FilterFlags.DECODE),
                        this.isSymbolic(), this.getEncoding());
                return this.fontProgram;
            } catch (IOException e) {
                LOGGER.error("Can't read TrueType font program.");
            }
        } else if (fontDescriptor.knownKey(ASAtom.FONT_FILE3)) {
            try {
                COSStream fontFile =
                        getStreamFromObject(fontDescriptor.getKey(ASAtom.FONT_FILE3));
                COSName subtype = (COSName) fontFile.getKey(ASAtom.SUBTYPE).get();
                if (ASAtom.CID_FONT_TYPE0C == subtype.get()) {
                    this.fontProgram = new CFFFontProgram(
                            fontFile.getData(COSStream.FilterFlags.DECODE));
                    return this.fontProgram;
                } else if (ASAtom.OPEN_TYPE == subtype.get()) {
                    ASAtom fontName = this.getFontName();
                    if (fontName == ASAtom.TRUE_TYPE || fontName == ASAtom.CID_FONT_TYPE2) {
                        this.fontProgram = new OpenTypeFontProgram(
                                fontFile.getData(COSStream.FilterFlags.DECODE),
                                false, this.isSymbolic(), this.getEncoding());
                        return this.fontProgram;
                    } else {
                        this.fontProgram = new OpenTypeFontProgram(
                                fontFile.getData(COSStream.FilterFlags.DECODE),
                                true, this.isSymbolic(), this.getEncoding());
                        return this.fontProgram;
                    }
                }
            } catch (IOException e) {
                LOGGER.error("Can't read CFF font program.");
            }
        }
        this.fontProgram = null;
        return null;
    }
}
