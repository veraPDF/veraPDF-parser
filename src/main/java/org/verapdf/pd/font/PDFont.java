package org.verapdf.pd.font;

import org.apache.log4j.Logger;
import org.verapdf.as.ASAtom;
import org.verapdf.cos.*;
import org.verapdf.pd.PDResource;
import org.verapdf.pd.font.cmap.PDCMap;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * This is PD representation of font.
 *
 * @author Sergey Shemyakov
 */
public abstract class PDFont extends PDResource {

    private static final Logger LOGGER = Logger.getLogger(PDFont.class);

    protected COSDictionary dictionary;
    protected COSDictionary fontDescriptor;
    protected PDCMap toUnicodeCMap;

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

    public Encoding getEncodingMapping() {
        COSBase encoding = this.getEncoding().getDirectBase();
        if (encoding.getType() == COSObjType.COS_NAME) {
            return new Encoding(encoding.getName());
        } else if (encoding.getType() == COSObjType.COS_DICT) {
            return new Encoding(encoding.getNameKey(ASAtom.BASE_ENCODING),
                    this.getDifferences());
        } else {
            return null;
        }
    }

    public String getName() {
        return this.dictionary.getStringKey(ASAtom.BASE_FONT);
    }

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

    /**
     * Method reads next character code from stream according to font data. It
     * can contain from 1 to 4 bytes.
     *
     * @param stream is stream with raw data.
     * @return next character code read.
     * @throws IOException if reading fails.
     */
    public int readCode(InputStream stream) throws IOException {
        return stream.read();
    }

    public abstract FontProgram getFontProgram();

    /**
     * Gets Unicode string for given character code. This method returns null in
     * case when no toUnicode mapping for this character was found, so some
     * inherited classes need to call this method, check return value on null
     * and then implement their special logic.
     *
     * @param code is code for character.
     * @return Unicode string
     */
    public String toUnicode(int code) {

        if (toUnicodeCMap == null) {
            this.toUnicodeCMap = new PDCMap(this.dictionary.getKey(ASAtom.TO_UNICODE));
        }

        if (toUnicodeCMap.getCMapName() != null &&
                toUnicodeCMap.getCMapName().startsWith("Identity-")) {
            return new String(new char[]{(char) code});
        }
        return this.toUnicodeCMap.toUnicode(code);
    }

    public double getWidth(int code) {
        if (dictionary.knownKey(ASAtom.WIDTHS) ||
                dictionary.knownKey(ASAtom.MISSING_WIDTH)) {
            int firstChar = dictionary.getIntegerKey(ASAtom.FIRST_CHAR).intValue();
            int lastChar = dictionary.getIntegerKey(ASAtom.LAST_CHAR).intValue();
            if (getWidths().size() > 0 && code >= firstChar && code <= lastChar) {
                return getWidths().at(code - firstChar).getReal();
            }

            if (this.fontDescriptor != null) {
                return fontDescriptor.getRealKey(ASAtom.MISSING_WIDTH);
            }
        }
        // TODO: process case of standard fonts

        try {
            this.getFontProgram().parseFont();
            return this.getFontProgram().getWidth(code);
        } catch (IOException e) {
            LOGGER.warn("Can't parse font program of font " + this.getName());
            return 0;
        }
    }
}
