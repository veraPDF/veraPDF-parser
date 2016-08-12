package org.verapdf.pd;

import org.verapdf.as.ASAtom;
import org.verapdf.cos.COSDictionary;
import org.verapdf.cos.COSObjType;
import org.verapdf.cos.COSObject;
import org.verapdf.font.PDFlibFont;

/**
 * This is PD representation of font.
 *
 * @author Sergey Shemyakov
 */
public abstract class PDFont {

    private COSDictionary dictionary;
    private COSDictionary fontDescriptor;

    /**
     * Constructor from COSDictionary.
     *
     * @param dictionary is font dictionary.
     */
    public PDFont(COSDictionary dictionary) {
        this.dictionary = dictionary;
        COSObject fd = dictionary.getKey(ASAtom.FONT_DESC);
        if(fd.getType() == COSObjType.COS_DICT) {
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
        if (this.getFontDescriptor() != null) {
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
        if(flagsLong == null) {
            throw new IllegalStateException("Font descriptor doesn't contain /Flags entry");
        }
        int flags = flagsLong.intValue();
        return (flags & 0b00100100) == 4;
    }

    public abstract String getName();

    public PDFlibFont getFontFile() {   // TODO: fix A LOT of details of this method.
        return null;    // the idea on moment of creation is as follows: we need some method in GFPDTrueType that can
    }   // give us information about TrueType cmap (3,1)
}
