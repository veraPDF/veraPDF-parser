package org.verapdf.pd.font;

import org.apache.log4j.Logger;
import org.verapdf.as.ASAtom;
import org.verapdf.cos.*;
import org.verapdf.pd.font.cff.CFFFontProgram;
import org.verapdf.pd.font.truetype.TrueTypeFontProgram;
import org.verapdf.pd.font.type1.Type1FontProgram;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * This is PD representation of font.
 *
 * @author Sergey Shemyakov
 */
public class PDFont {

    private static final Logger LOGGER = Logger.getLogger(PDFont.class);

    protected COSDictionary dictionary;
    protected COSDictionary fontDescriptor;

    /**
     * Constructor from COSDictionary.
     *
     * @param dictionary is font dictionary.
     */
    public PDFont(COSDictionary dictionary) {
        this.dictionary = dictionary;
        COSObject fd = dictionary.getKey(ASAtom.FONT_DESC);
        if (fd.getType() == COSObjType.COS_DICT) {
            fontDescriptor = (COSDictionary) fd.getDirectBase();
        } else {
            fontDescriptor = null;
        }
    }

    /**
     * @return font COSDictionary.
     */
    public COSDictionary getDictionary() {
        return dictionary;
    }

    /**
     * @return font descriptor COSDictionary.
     */
    public COSDictionary getFontDescriptor() {
        return fontDescriptor;
    }

    /**
     * @return font type (Type entry).
     */
    public String getType() {
        String type = this.dictionary.getStringKey(ASAtom.TYPE);
        return type == null ? "" : type;
    }

    /**
     * @return font subtype (Subtype entry).
     */
    public String getSubtype() {
        String type = this.dictionary.getStringKey(ASAtom.SUBTYPE);
        return type == null ? "" : type;
    }

    /**
     * @return font name defined by BaseFont entry in the font dictionary and
     * FontName key in the font descriptor.
     * @throws IllegalStateException if font names specified in font dictionary
     *                               and font descriptor are different.
     */
    public String getFontName() throws IllegalStateException {
        String type = this.dictionary.getStringKey(ASAtom.BASE_FONT);
        if (this.fontDescriptor != null) {
            String typeFromDescriptor =
                    this.fontDescriptor.getStringKey(ASAtom.FONT_NAME);
            if (!type.equals(typeFromDescriptor)) {
                throw new IllegalStateException("Font names specified in font dictionary and font descriptor are different");
            }
        }
        return type == null ? "" : type;
    }

    /**
     * @return true if the font flags in the font descriptor dictionary mark
     * indicate that the font is symbolic (the entry /Flags has bit 3 set to 1
     * and bit 6 set to 0).
     * @throws IllegalStateException if these flags are set to the same value,
     *                               i. e. are both 1 or both 0, or if font
     *                               descriptor is null.
     */
    public boolean isSymbolic() throws IllegalStateException {
        if (this.fontDescriptor == null) {
            throw new IllegalStateException("Font descriptor is null");
        }
        Long flagsLong = this.fontDescriptor.getIntegerKey(ASAtom.FLAGS);
        if (flagsLong == null) {
            throw new IllegalStateException("Font descriptor doesn't contain /Flags entry");
        }
        int flags = flagsLong.intValue();
        return (flags & 0b00100100) == 4;
    }

    public String getName() {
        return this.dictionary.getStringKey(ASAtom.BASE_FONT);
    }

    public PDFLibFontProgram getFontFile() {
        if (fontDescriptor.knownKey(ASAtom.FONT_FILE)) {
            COSStream type1FontFile =
                    (COSStream) fontDescriptor.getKey(ASAtom.FONT_FILE).get();
            try {
                return new Type1FontProgram(
                        type1FontFile.getData(COSStream.FilterFlags.DECODE));
            } catch (IOException e) {
                LOGGER.error("Can't read Type 1 font program.");
            }
        } else if (fontDescriptor.knownKey(ASAtom.FONT_FILE2)) {
            COSStream trueTypeFontFile =
                    (COSStream) fontDescriptor.getKey(ASAtom.FONT_FILE2).get();
            try {
                return new TrueTypeFontProgram(trueTypeFontFile.getData(COSStream.FilterFlags.DECODE),
                        this.isSymbolic(), this.dictionary.getKey(ASAtom.ENCODING));
            } catch (IOException e) {
                LOGGER.error("Can't read TrueType font program.");
            }
        } else if (fontDescriptor.knownKey(ASAtom.FONT_FILE3)) {
            COSStream fontFile =
                    (COSStream) fontDescriptor.getKey(ASAtom.FONT_FILE3).get();
            COSName subtype = (COSName) fontFile.getKey(ASAtom.SUBTYPE).get();
            if (ASAtom.TYPE1C.equals(subtype.get()) ||
                    ASAtom.CID_FONT_TYPE0C.equals(subtype.get())) {     // TODO: check if cases of CFF type 1 and CFF CID are fine
                try {
                    return new CFFFontProgram(fontFile.getData(COSStream.FilterFlags.DECODE));
                } catch (IOException e) {
                    LOGGER.error("Can't read CFF font program.");
                }
            } else if (ASAtom.OPEN_TYPE.equals(subtype.get())) {
                return null;    // TODO: add OpenType
            }
        }
        return null;
    }

    public COSObject getEncoding() {
        return this.dictionary.getKey(ASAtom.ENCODING);
    }

    public Map<Integer, String> getDifferences() {
        COSObject encoding = this.getEncoding();
        COSArray differences = (COSArray) encoding.getKey(ASAtom.DIFFERENCES).get();
        if (differences == null) {
            return null;
        }
        Map<Integer, String> res = new HashMap<>();
        int diffIndex = 0;
        for (COSObject obj : differences) {
            if (obj.getType() == COSObjType.COS_INTEGER) {
                diffIndex = obj.getInteger().intValue();
            } else if (obj.getType() == COSObjType.COS_NAME && diffIndex != -1) {
                res.put(diffIndex++, obj.getString());
            }
        }
        return res;
    }
}
