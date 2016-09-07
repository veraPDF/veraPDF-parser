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
 * Instance of this class contains True Type Font data.
 *
 * @author Sergey Shemyakov
 */
public class TrueTypeFontProgram implements FontProgram {

    private float[] widths;

    private TrueTypeFontParser parser;
    private boolean isSymbolic;
    private COSObject encoding;
    private String[] encodingMappingArray;

    /**
     * Constructor from stream, containing font data, and encoding details.
     *
     * @param stream is stream containing font data.
     * @param isSymbolic is true if font is marked as symbolic.
     * @param encoding is value of /Encoding in font dictionary.
     * @throws IOException if creation of @{link InternalInputStream} fails.
     */
    public TrueTypeFontProgram(ASInputStream stream, boolean isSymbolic,
                               COSObject encoding) throws IOException {
        this.parser = new TrueTypeFontParser(stream);
        this.isSymbolic = isSymbolic;
        if (encoding != null) {
            this.encoding = encoding;
        } else {
            this.isSymbolic = true;
        }
    }

    /**
     * Parses True Type font from given stream and extracts all the data needed.
     *
     * @throws IOException if stream-reading error occurs.
     */
    @Override
    public void parseFont() throws IOException {
        this.parser.readHeader();
        this.parser.readTableDirectory();
        this.parser.readTables();

        float quotient = 1000f / this.parser.getHeadParser().getUnitsPerEm();
        int[] unconvertedWidths = this.parser.getHmtxParser().getLongHorMetrics();
        widths = new float[unconvertedWidths.length];
        for (int i = 0; i < unconvertedWidths.length; ++i) {
            widths[i] = unconvertedWidths[i] * quotient;
        }

        if (!isSymbolic) {
            this.createCIDToNameTable();
        }
    }

    /**
     * @return array, containing platform ID and encoding ID for each cmap in
     * this True Type font.
     */
    public TrueTypeCmapSubtable[] getCmapEncodingPlatform() {
        return this.parser.getCmapParser().getCmapInfos();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean containsCID(int cid) {
        for (TrueTypeCmapSubtable cMap : getCmapEncodingPlatform()) {
            if (cMap.containsCID(cid)) {
                return true;
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
            String glyphName = encodingMappingArray[code];
            return getWidth(glyphName);
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
        if (glyphName.equals(TrueTypePredefined.NOTDEF_STRING)) {
            int gid = this.parser.getPostParser().getGID(glyphName);
            return getWidthWithCheck(gid);
        }
        TrueTypeCmapSubtable cmap31 = this.parser.getCmapTable(3, 1);
        if (cmap31 != null) {
            AdobeGlyphList.AGLUnicode unicode = AdobeGlyphList.get(glyphName);
            int gid = cmap31.getGlyph(unicode.getSymbolCode());
            return getWidthWithCheck(gid);
        } else {
            TrueTypeCmapSubtable cmap10 = this.parser.getCmapTable(1, 0);
            if (cmap10 != null) {
                int charCode = TrueTypePredefined.MAC_OS_ROMAN_ENCODING_MAP.get(glyphName);
                int gid = cmap10.getGlyph(charCode);
                return getWidthWithCheck(gid);
            } else {
                return -1;  //case when no cmap (3,1) and no (1,0) is found
            }
        }
    }

    /**
     * @return true if font is symbolic.
     */
    public boolean isSymbolic() {
        return isSymbolic;
    }

    /**
     * @return number of glyphs in this font.
     */
    public int getNGlyphs() {
        return this.parser.getMaxpParser().getNumGlyphs();
    }

    /**
     * Returns true if cmap table with given platform ID and encoding ID is
     * present in the font.
     *
     * @param platformID is platform ID of requested cmap.
     * @param encodingID is encoding ID of requested cmap.
     * @return true if requested cmap is present.
     */
    public boolean isCmapPresent(int platformID, int encodingID) {
        return this.parser.getCmapTable(platformID, encodingID) != null;
    }

    private float getWidthSymbolic(int code) {
        TrueTypeCmapSubtable cmap30 = this.parser.getCmapTable(3, 0);
        if (cmap30 != null) {
            int sampleCode = cmap30.getSampleCharCode();
            int highByteMask = sampleCode & 0x0000FF00;
            if (highByteMask != 0x00000000 && highByteMask != 0x0000F000 &&
                    highByteMask != 0x0000F100 && highByteMask != 0x0000F200) { // should we check this at all?
                return -1;
            }
            int gid = cmap30.getGlyph(highByteMask & code);     // we suppose that code is in fact 1-byte value
            return getWidthWithCheck(gid);
        } else {
            TrueTypeCmapSubtable cmap10 = this.parser.getCmapTable(1, 0);
            if (cmap10 != null) {
                int gid = cmap10.getGlyph(code);
                return getWidthWithCheck(gid);
            } else {
                return -1;
            }
        }
    }

    private float getWidthWithCheck(int gid) {
        if (gid < widths.length) {
            return widths[gid];
        } else {
            return widths[widths.length - 1];   // case of monospaced fonts
        }
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
            createCIDToNameTableFromDict((COSDictionary) this.encoding.get());
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
        COSArray differences = (COSArray) encoding.getKey(ASAtom.DIFFERENCES).get();
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
