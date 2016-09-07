package org.verapdf.pd.font;

import org.apache.log4j.Logger;
import org.verapdf.as.ASAtom;
import org.verapdf.cos.COSDictionary;
import org.verapdf.pd.font.truetype.PDTrueTypeFont;
import org.verapdf.pd.font.type1.PDType1Font;

/**
 * Creates PDFont from COSObject that is font dictionary.
 *
 * @author Sergey Shemyakov
 */
public class PDFontFactory {

    private static final Logger LOGGER = Logger.getLogger(PDFontFactory.class);

    private PDFontFactory() {
    }

    /**
     * @param fontDictionary is COSDictionary that is font dictionary.
     * @return PDFont that corresponds to this dictionary.
     */
    public static PDFont getPDFont(COSDictionary fontDictionary) {
        ASAtom subtype = fontDictionary.getNameKey(ASAtom.SUBTYPE);
        if (subtype == ASAtom.TYPE1) {
            return new PDType1Font(fontDictionary);
        } else if (subtype == ASAtom.TRUE_TYPE ||
                subtype == ASAtom.MM_TYPE1) {
            return new PDTrueTypeFont(fontDictionary);
        } else if (subtype == ASAtom.TYPE3) {
            return new PDType3Font(fontDictionary);
        } else if (subtype == ASAtom.TYPE0) {
            return new PDType0Font(fontDictionary);
        } else if (subtype == ASAtom.CID_FONT_TYPE0 ||
                subtype == ASAtom.CID_FONT_TYPE2) {
            return new PDCIDFont(fontDictionary);
        } else {
            LOGGER.error("Invalid value of Subtype in font dictionary");
            return null;
        }
    }
}
