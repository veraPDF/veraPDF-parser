package org.verapdf.pd.font.truetype;

import org.verapdf.as.ASAtom;
import org.verapdf.as.io.ASInputStream;
import org.verapdf.cos.COSArray;
import org.verapdf.cos.COSDictionary;
import org.verapdf.cos.COSObjType;
import org.verapdf.cos.COSObject;
import org.verapdf.pd.font.FontProgram;

import java.io.IOException;

/**
 * Represents TrueTypeFontProgram.
 *
 * @author Sergey Shemyakov
 */
public class TrueTypeFontProgram extends BaseTrueTypeProgram implements FontProgram {

    private COSObject encoding;
    protected boolean isSymbolic;

    /**
     * Constructor from stream containing font data, and encoding details.
     *
     * @param stream     is stream containing font data.
     * @param isSymbolic is true if font is marked as symbolic.
     * @param encoding   is value of /Encoding in font dictionary.
     * @throws IOException if creation of @{link SeekableStream} fails.
     */
    public TrueTypeFontProgram(ASInputStream stream, boolean isSymbolic,
                               COSObject encoding) throws IOException {
        super(stream);
        this.isSymbolic = isSymbolic;
        if (encoding != null) {
            this.encoding = encoding;
        }
    }

    @Override
    public void parseFont() throws IOException {
        super.parseFont();
        if (!isSymbolic) {
            this.createCIDToNameTable();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean containsCode(int code) {
        if (!isSymbolic) {
            String glyph;
            if (this.encodingMappingArray != null && code < this.encodingMappingArray.length) {
                glyph = this.encodingMappingArray[code];
            } else {
                glyph = TrueTypePredefined.NOTDEF_STRING;
            }
            if (TrueTypePredefined.NOTDEF_STRING.equals(glyph)) {
                return false;
            }
            AdobeGlyphList.AGLUnicode unicode = AdobeGlyphList.get(glyph);
            TrueTypeCmapSubtable cmap31 = this.parser.getCmapTable(3, 1);
            if (cmap31 != null) {
                if (cmap31.getGlyph(unicode.getSymbolCode()) != 0) {
                    return true;
                }
            }
            TrueTypeCmapSubtable cmap10 = this.parser.getCmapTable(1, 0);
            if (cmap10 != null) {
                int charCode = TrueTypePredefined.MAC_OS_ROMAN_ENCODING_MAP.get(glyph);
                return cmap10.getGlyph(charCode) != 0;
            }
        } else {
            int gid = getGIDFrom30(code);
            if (gid != 0) {
                return true;
            }
            TrueTypeCmapSubtable cmap10 = this.parser.getCmapTable(1, 0);
            if (cmap10 != null) {
                return cmap10.getGlyph(code) != 0;
            }
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public float getWidth(int code) {
        if (isSymbolic) {
            return getWidthSymbolic(code);
        } else {
            if (encodingMappingArray == null) {  // no external encoding
                int gid = this.parser.getCmapParser().getGID(code);
                return getWidthWithCheck(gid);
            }
            if (code < 256) {
                String glyphName = encodingMappingArray[code];
                return getWidth(glyphName);
            } else {
                return getWidth(TrueTypePredefined.NOTDEF_STRING);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public float getWidth(String glyphName) {
        if (isSymbolic) {
            return -1;
        }
        if (TrueTypePredefined.NOTDEF_STRING.equals(glyphName)) {
            int gid = this.parser.getPostParser().getGID(glyphName);
            return getWidthWithCheck(gid);
        }
        TrueTypeCmapSubtable cmap31 = this.parser.getCmapTable(3, 1);
        if (cmap31 != null) {
            AdobeGlyphList.AGLUnicode unicode = AdobeGlyphList.get(glyphName);
            int gid = cmap31.getGlyph(unicode.getSymbolCode());
            if (gid != 0) {
                return getWidthWithCheck(gid);
            }
        }
        TrueTypeCmapSubtable cmap10 = this.parser.getCmapTable(1, 0);
        if (cmap10 != null) {
            int charCode = TrueTypePredefined.MAC_OS_ROMAN_ENCODING_MAP.get(glyphName);
            int gid = cmap10.getGlyph(charCode);
            return getWidthWithCheck(gid);
        } else {
            return -1;  //case when no cmap (3,1) and no (1,0) is found
        }
    }

    /**
     * @return true if font is symbolic.
     */
    public boolean isSymbolic() {
        return isSymbolic;
    }

    private float getWidthSymbolic(int code) {
        int gid = getGIDFrom30(code);
        if (gid != 0) {
            return getWidthWithCheck(gid);
        }

        TrueTypeCmapSubtable cmap10 = this.parser.getCmapTable(1, 0);
        if (cmap10 != null) {
            gid = cmap10.getGlyph(code);
            return getWidthWithCheck(gid);
        }
        return -1;
    }

    private int getGIDFrom30(int code) {
        TrueTypeCmapSubtable cmap30 = this.parser.getCmapTable(3, 0);
        int gid;
        if (cmap30 != null) {
            int sampleCode = cmap30.getSampleCharCode();
            int highByteMask = sampleCode & 0x0000FF00;

            if (highByteMask == 0x00000000 || highByteMask == 0x0000F000 ||
                    highByteMask == 0x0000F100 || highByteMask == 0x0000F200) { // should we check this at all?
                gid = cmap30.getGlyph(highByteMask & code);     // we suppose that code is in fact 1-byte value
                return gid;
            }
        }
        return 0;
    }

    private void createCIDToNameTable() throws IOException {
        this.encodingMappingArray = new String[256];
        if (this.encoding.getType() == COSObjType.COS_NAME) {
            if (ASAtom.MAC_ROMAN_ENCODING.getValue().equals(this.encoding.get().getString())) {
                System.arraycopy(TrueTypePredefined.MAC_ROMAN_ENCODING, 0,
                        encodingMappingArray, 0, 256);
            } else if (ASAtom.WIN_ANSI_ENCODING.getValue().equals(this.encoding.get().getString())) {
                System.arraycopy(TrueTypePredefined.WIN_ANSI_ENCODING, 0,
                        encodingMappingArray, 0, 256);
            } else {
                throw new IOException("Error in reading /Encoding entry in font dictionary");
            }
        } else if (this.encoding.getType() == COSObjType.COS_DICT) {
            createCIDToNameTableFromDict((COSDictionary) this.encoding.getDirectBase());
        } else {
            throw new IOException("Error in reading /Encoding entry in font dictionary");
        }
    }

    private void createCIDToNameTableFromDict(COSDictionary encoding) throws IOException {
        if (encoding.knownKey(ASAtom.BASE_ENCODING)) {
            ASAtom baseEncoding = encoding.getNameKey(ASAtom.BASE_ENCODING);
            if (ASAtom.WIN_ANSI_ENCODING.equals(baseEncoding)) {
                System.arraycopy(TrueTypePredefined.WIN_ANSI_ENCODING, 0,
                        encodingMappingArray, 0, 256);
            } else if (ASAtom.MAC_ROMAN_ENCODING.equals(baseEncoding)) {
                System.arraycopy(TrueTypePredefined.MAC_ROMAN_ENCODING, 0,
                        encodingMappingArray, 0, 256);
            } else if (ASAtom.getASAtom(
                    TrueTypePredefined.MAC_EXPERT_ENCODING_STRING).equals(baseEncoding)) {
                System.arraycopy(TrueTypePredefined.MAC_EXPERT_ENCODING, 0,
                        encodingMappingArray, 0, 256);
            } else {
                throw new IOException("Error in reading /Encoding entry in font dictionary");
            }
        } else {
            System.arraycopy(TrueTypePredefined.STANDARD_ENCODING, 0,
                    encodingMappingArray, 0, 256);
        }
        COSArray differences = (COSArray) encoding.getKey(ASAtom.DIFFERENCES).getDirectBase();
        if (differences != null) {
            applyDiffsToEncoding(differences);
        }
        for (int i = 0; i < 256; ++i) {
            if (TrueTypePredefined.NOTDEF_STRING.equals(encodingMappingArray[i])) {
                encodingMappingArray[i] = TrueTypePredefined.STANDARD_ENCODING[i];
            }
        }
    }

    private void applyDiffsToEncoding(COSArray differences) throws IOException {
        int diffIndex = -1;
        for (COSObject obj : differences) {
            if (obj.getType() == COSObjType.COS_INTEGER) {
                diffIndex = obj.getInteger().intValue();
            } else if (obj.getType() == COSObjType.COS_NAME && diffIndex != -1) {
                encodingMappingArray[diffIndex++] = obj.getString();
            } else {
                throw new IOException("Error in reading /Encoding entry in font dictionary");
            }
        }
    }
}
