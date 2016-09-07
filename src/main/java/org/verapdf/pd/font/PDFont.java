package org.verapdf.pd.font;

import org.apache.log4j.Logger;
import org.verapdf.as.ASAtom;
import org.verapdf.cos.*;

import java.util.HashMap;
import java.util.Map;

/**
 * This is PD representation of font.
 *
 * @author Sergey Shemyakov
 */
public abstract class PDFont {

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
    public ASAtom getSubtype() {
        return this.dictionary.getNameKey(ASAtom.SUBTYPE);
    }

    /**
     * @return font name defined by BaseFont entry in the font dictionary and
     * FontName key in the font descriptor.
     * @throws IllegalStateException if font names specified in font dictionary
     *                               and font descriptor are different.
     */
    public ASAtom getFontName() throws IllegalStateException {
        ASAtom type = this.dictionary.getNameKey(ASAtom.BASE_FONT);
        if (this.fontDescriptor != null && type != null) {
            ASAtom typeFromDescriptor =
                    this.fontDescriptor.getNameKey(ASAtom.FONT_NAME);
            if (type != typeFromDescriptor) {
                throw new IllegalStateException("Font names specified in font dictionary and font descriptor are different");
            }
        }
        return type;
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

    public abstract FontProgram getFontProgram();

    public COSObject getEncoding() {
        return this.dictionary.getKey(ASAtom.ENCODING);
    }

    public COSStream getFontFile2() {
        return (COSStream) this.fontDescriptor.getKey(ASAtom.FONT_FILE2).get();
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

    public COSObject getWidths() {
        return this.dictionary.getKey(ASAtom.WIDTHS);
    }

    public Long getFirstChar() {
        return this.dictionary.getIntegerKey(ASAtom.FIRST_CHAR);
    }

    public Long getLastChar() {
        return this.dictionary.getIntegerKey(ASAtom.LAST_CHAR);
    }
}
