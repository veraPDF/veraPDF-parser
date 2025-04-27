/**
 * This file is part of veraPDF Parser, a module of the veraPDF project.
 * Copyright (c) 2015-2025, veraPDF Consortium <info@verapdf.org>
 * All rights reserved.
 *
 * veraPDF Parser is free software: you can redistribute it and/or modify
 * it under the terms of either:
 *
 * The GNU General public license GPLv3+.
 * You should have received a copy of the GNU General Public License
 * along with veraPDF Parser as the LICENSE.GPL file in the root of the source
 * tree.  If not, see http://www.gnu.org/licenses/ or
 * https://www.gnu.org/licenses/gpl-3.0.en.html.
 *
 * The Mozilla Public License MPLv2+.
 * You should have received a copy of the Mozilla Public License along with
 * veraPDF Parser as the LICENSE.MPL file in the root of the source tree.
 * If a copy of the MPL was not distributed with this file, you can obtain one at
 * http://mozilla.org/MPL/2.0/.
 */
package org.verapdf.pd.font.truetype;

import org.verapdf.as.ASAtom;
import org.verapdf.as.io.ASInputStream;
import org.verapdf.cos.COSDictionary;
import org.verapdf.cos.COSKey;
import org.verapdf.cos.COSObjType;
import org.verapdf.cos.COSObject;
import org.verapdf.pd.font.FontProgram;
import org.verapdf.pd.font.PDFont;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Represents TrueTypeFontProgram.
 *
 * @author Sergey Shemyakov
 */
public class TrueTypeFontProgram extends BaseTrueTypeProgram implements FontProgram {

    private static final Logger LOGGER = Logger.getLogger(TrueTypeFontProgram.class.getCanonicalName());

    private COSObject encoding;
    private final boolean isSymbolic;

    /**
     * Constructor from stream containing font data, and encoding details.
     *
     * @param stream     is stream containing font data.
     * @param isSymbolic is true if font is marked as symbolic.
     * @param encoding   is value of /Encoding in font dictionary.
     * @throws IOException if creation of @{link SeekableStream} fails.
     */
    public TrueTypeFontProgram(ASInputStream stream, boolean isSymbolic,
                               COSObject encoding, COSKey key) throws IOException {
        super(stream, key);
        this.isSymbolic = isSymbolic;
        if (encoding != null) {
            this.encoding = encoding;
        }
    }

    @Override
    public void parseFont() throws IOException {
        super.parseFont();
        if (!isSymbolic && encoding.getDirectBase() != null && encodingMappingArray == null) {
            this.createCIDToNameTable();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean containsCode(int code) {
        if (!isSymbolic && encoding.getDirectBase() != null) {
            String glyph;
            if (this.encodingMappingArray != null && code < this.encodingMappingArray.length) {
                glyph = this.encodingMappingArray[code];
            } else {
                glyph = TrueTypePredefined.NOTDEF_STRING;
            }
            if (TrueTypePredefined.NOTDEF_STRING.equals(glyph)) {
                return false;
            }
            int gid = getGidFromCMaps(glyph);
            if (gid == 0) {
                // if cmap lookup fails we go to post table
                gid = getGidFromPostTable(glyph);
            }
            return gid > 0 && gid < getNGlyphs();
        } else {
            if (isCmapPresent(3, 0)) {
                //No need to look at cmap(1, 0) if cmap(3, 0) exist
                //GID 0 is considered to be .notdef
                return cMap30containsGlyph(code) && getGIDFrom30(code) != 0;
            }
            TrueTypeCmapSubtable cmap10 = this.parser.getCmapTable(1, 0);
            //GID 0 is considered to be .notdef
            return cmap10 != null && cmap10.containsCID(code) && cmap10.getGlyph(code) != 0;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public float getWidth(int code) {
        if (isSymbolic || encoding.getDirectBase() == null) {
            return getWidthSymbolic(code);
        } else {
            float width;
            if (encodingMappingArray != null && code < encodingMappingArray.length) {
                String glyphName = encodingMappingArray[code];
                width = getWidth(glyphName);
            } else {
                width = getWidth(TrueTypePredefined.NOTDEF_STRING);
            }
            if (width == -1) {
                int gid = this.parser.getCmapParser().getGID(code);
                return getWidthWithCheck(gid);
            }
            return width;
        }
    }

    @Override
    public String getGlyphName(int code) {
        if (!isSymbolic && encoding.getDirectBase() != null && encodingMappingArray != null &&
                code < encodingMappingArray.length) {
            return encodingMappingArray[code];
        } else if (isSymbolic || encoding.getDirectBase() == null) {
            return " "; // indicates that toUnicode should not be checked.
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public float getWidth(String glyphName) {
        if (isSymbolic || encoding.getDirectBase() == null) {
            return -1;
        }
        int gid = getGidFromCMaps(glyphName);
        if (gid == 0) {
            // if cmap lookup fails we go to post table
            gid = getGidFromPostTable(glyphName);
        }
        if (mappingForGlyphIsPresent(glyphName)) {
            return getWidthWithCheck(gid);
        } else {
            return -1;  //case when no cmap (3,1) and no (1,0) is found
        }
    }

    private boolean mappingForGlyphIsPresent(String glyphName) {
        TrueTypePostTable postTable = this.parser.getPostParser();
        return getNrOfCMaps() != 0 || (postTable != null && postTable.containsGlyph(glyphName));
    }

    @Override
    public boolean containsGlyph(String glyphName) {
        if (!isSymbolic && encoding.getDirectBase() != null) {
            int gid = getGidFromCMaps(glyphName);
            return gid >= 0 && gid < getNGlyphs();
        } else {
            return false;   // no notation of glyph name for symbolic fonts?
        }
    }

    private int getGidFromCMaps(String glyphName) {
        AdobeGlyphList.AGLUnicode unicode = AdobeGlyphList.get(glyphName);
        TrueTypeCmapSubtable cmap31 = this.parser.getCmapTable(3, 1);
        if (cmap31 != null) {
            int gid = cmap31.getGlyph(unicode.getSymbolCode());
            if (gid != 0) {
                return gid;
            }
        }
        TrueTypeCmapSubtable cmap10 = this.parser.getCmapTable(1, 0);
        if (cmap10 != null) {
            Integer charCode = TrueTypePredefined.MAC_OS_ROMAN_ENCODING_MAP.get(glyphName);
            if (charCode != null) {
                return cmap10.getGlyph(charCode);
            }
        }
        return 0;
    }

    private int getGidFromPostTable(String glyphName) {
        TrueTypePostTable postTable = this.parser.getPostParser();
        return postTable == null ? 0 : postTable.getGID(glyphName);
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
        gid = this.parser.getCmapParser().getGID(code);
        return getWidthWithCheck(gid);
    }

    private int getGIDFrom30(int code) {
        TrueTypeCmapSubtable cmap30 = this.parser.getCmapTable(3, 0);
        if (cmap30 != null) {
            int sampleCode = cmap30.getSampleCharCode();
            int highByteMask = sampleCode & 0x0000FF00;

            if (highByteMask == 0x00000000 || highByteMask == 0x0000F000 ||
                    highByteMask == 0x0000F100 || highByteMask == 0x0000F200) { // should we check this at all?
                // we suppose that code is in fact 1-byte value
                int gid = cmap30.getGlyph(highByteMask + code);
                return gid;
            }
        }
        return 0;
    }

    private boolean cMap30containsGlyph(int code) {
        TrueTypeCmapSubtable cmap30 = this.parser.getCmapTable(3, 0);
        if (cmap30 != null) {
            int sampleCode = cmap30.getSampleCharCode();
            int highByteMask = sampleCode & 0x0000FF00;
            if (highByteMask == 0x00000000 || highByteMask == 0x0000F000 ||
                    highByteMask == 0x0000F100 || highByteMask == 0x0000F200) { // should we check this at all?
                return cmap30.containsCID(highByteMask + code);     // we suppose that code is in fact 1-byte value
            }
        }
        return false;
    }

    private void createCIDToNameTable() throws IOException {
        this.encodingMappingArray = new String[256];
        if (this.encoding.getType() == COSObjType.COS_NAME) {
            if (ASAtom.MAC_ROMAN_ENCODING.getValue().equals(this.encoding.getString())) {
                System.arraycopy(TrueTypePredefined.MAC_ROMAN_ENCODING, 0,
                        encodingMappingArray, 0, 256);
            } else if (ASAtom.WIN_ANSI_ENCODING.getValue().equals(this.encoding.getString())) {
                System.arraycopy(TrueTypePredefined.WIN_ANSI_ENCODING, 0,
                        encodingMappingArray, 0, 256);
            } else {
                throw new IOException(getErrorMessage("Error in reading /Encoding entry in font dictionary"));
            }
        } else if (this.encoding.getType() == COSObjType.COS_DICT) {
            createCIDToNameTableFromDict((COSDictionary) this.encoding.getDirectBase());
        } else {
            throw new IOException(getErrorMessage("Error in reading /Encoding entry in font dictionary"));
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
                LOGGER.log(Level.SEVERE, getErrorMessage("Error in reading /Encoding entry in font dictionary"));
                System.arraycopy(TrueTypePredefined.STANDARD_ENCODING, 0,
                        encodingMappingArray, 0, 256);
            }
        } else {
            System.arraycopy(TrueTypePredefined.STANDARD_ENCODING, 0,
                    encodingMappingArray, 0, 256);
        }

        applyDiffsToEncoding(encoding);

        for (int i = 0; i < 256; ++i) {
            if (TrueTypePredefined.NOTDEF_STRING.equals(encodingMappingArray[i])) {
                encodingMappingArray[i] = TrueTypePredefined.STANDARD_ENCODING[i];
            }
        }
    }

    private void applyDiffsToEncoding(COSDictionary encoding) throws IOException {
        Map<Integer, String> differences = PDFont.getDifferencesFromCosEncoding(new COSObject(encoding));
        if (differences != null) {
            for (Map.Entry<Integer, String> entry : differences.entrySet()) {
                int key = entry.getKey();
                if (key < encodingMappingArray.length) {
                    encodingMappingArray[key] = entry.getValue();
                }
            }
        } else {
            throw new IOException(getErrorMessage("Error in reading /Encoding entry in font dictionary"));
        }
    }

    @Override
    public boolean containsCID(int cid) {
        return false;
    }

    @Override
    public List<Integer> getCIDList() {
        return Collections.emptyList();
    }
}
