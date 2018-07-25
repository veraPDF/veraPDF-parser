/**
 * This file is part of veraPDF Parser, a module of the veraPDF project.
 * Copyright (c) 2015, veraPDF Consortium <info@verapdf.org>
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
package org.verapdf.pd.font;

import org.verapdf.as.ASAtom;
import org.verapdf.cos.COSArray;
import org.verapdf.cos.COSDictionary;
import org.verapdf.cos.COSName;
import org.verapdf.cos.COSObject;
import org.verapdf.pd.font.cmap.CMap;
import org.verapdf.pd.font.cmap.PDCMap;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Represents Type0 font on pd level. Note that on the cos level object of this
 * class is a COSDictionary of descendant font.
 *
 * @author Sergey Shemyakov
 */
public class PDType0Font extends PDCIDFont {

    private static final Logger LOGGER = Logger.getLogger(PDType0Font.class.getCanonicalName());
    private static final String UCS2 = "UCS2";
    private static final String IDENTITY_H = "Identity-H";
    private static final String IDENTITY_V = "Identity-V";
    private static final String JAPAN_1 = "Japan1";
    private static final String KOREA_1 = "Korea1";
    private static final String GB_1 = "GB1";
    private static final String CNS_1 = "CNS1";
    private static final String ADOBE = "Adobe";

    private PDCMap pdcMap;
    private PDCMap ucsCMap;
    private COSDictionary type0FontDict;

    /**
     * Constructs PD Type 0 font from font dictionary.
     * @param dictionary
     */
    public PDType0Font(COSDictionary dictionary) {
        super(getDescendantCOSDictionary(dictionary));
        type0FontDict = dictionary == null ?
                (COSDictionary) COSDictionary.construct().get() : dictionary;
        this.cMap = getCMap().getCMapFile();
    }

    /**
     * @return PD CMap associated with this Type 0 font as specified by Encoding
     * key in font dictionary.
     */
    public PDCMap getCMap() {
        if (this.pdcMap == null) {
            COSObject cMap = this.type0FontDict.getKey(ASAtom.ENCODING);
            if (!cMap.empty()) {
                org.verapdf.pd.font.cmap.PDCMap pdcMap = new org.verapdf.pd.font.cmap.PDCMap(cMap);
                this.pdcMap = pdcMap;
                return pdcMap;
            }
            return null;
        }
        return this.pdcMap;
    }

    private static COSDictionary getDescendantCOSDictionary(COSDictionary dict) {
        if (dict != null) {
            COSArray array =
                    (COSArray) dict.getKey(ASAtom.DESCENDANT_FONTS).getDirectBase();
            if (array != null) {
                return (COSDictionary) array.at(0).getDirectBase();
            }
        }
        return null;
    }

    /**
     * @return COSObject that is font dictionary for descendant font.
     */
    public COSObject getDescendantFontObject() {
        if (this.type0FontDict != null) {
            COSArray array = (COSArray) this.type0FontDict.getKey(ASAtom.DESCENDANT_FONTS).getDirectBase();
            if (array != null) {
                return array.at(0);
            }
        }
        return null;
    }

    /**
     * @return COSDictionary that is font dictionary for descendant font.
     */
    public COSDictionary getDescendantFont() {
        return getDescendantCOSDictionary(this.type0FontDict);
    }

    /**
     * This method maps character code to a Unicode value. Firstly it checks
     * toUnicode CMap, then it behaves like described in PDF32000_2008 9.10.2
     * "Mapping Character Codes to Unicode Values" for Type0 font.
     *
     * @param code is code for character.
     * @return unicode value.
     */
    @Override
    public String toUnicode(int code) {
        if (this.toUnicodeCMap == null) {
            this.toUnicodeCMap = new PDCMap(
                    this.type0FontDict.getKey(ASAtom.TO_UNICODE));
        }

        String unicode = super.toUnicode(code);
        if (unicode != null) {
            return unicode;
        }

        if (ucsCMap != null) {
            return ucsCMap.toUnicode(code);
        }

        if (IDENTITY_H.equals(pdcMap.getCMapName()) ||
                IDENTITY_V.equals(pdcMap.getCMapName())) {
            setUcsCMapFromIdentity(this.getCIDSystemInfo());
            if (this.ucsCMap == null) {
                LOGGER.log(Level.FINE, "Can't create toUnicode CMap from " + pdcMap.getCMapName());
                return null;
            }
            return ucsCMap.toUnicode(code);
        }
        PDCMap pdcMap = this.getCMap();
        if (pdcMap != null && pdcMap.getCMapFile() != null) {
            int cid = pdcMap.getCMapFile().toCID(code);
            String registry = pdcMap.getRegistry();
            String ordering = pdcMap.getOrdering();
            String ucsName = registry + "-" + ordering + "-" + UCS2;
            PDCMap pdUCSCMap = new PDCMap(COSName.construct(ucsName));
            CMap ucsCMap = pdUCSCMap.getCMapFile();
            if (ucsCMap != null) {
                this.ucsCMap = pdUCSCMap;
                return ucsCMap.getUnicode(cid);
            }
            LOGGER.log(Level.FINE, "Can't load CMap " + ucsName);
            return null;
        }
        LOGGER.log(Level.FINE, "Can't get CMap for font " + this.getName());
        return null;
    }

    private void setUcsCMapFromIdentity(PDCIDSystemInfo cidSystemInfo) {
        if (cidSystemInfo != null) {
            String registry = cidSystemInfo.getRegistry();
            if (ADOBE.equals(registry)) {
                String ordering = cidSystemInfo.getOrdering();
                if (JAPAN_1.equals(ordering) || CNS_1.equals(ordering) ||
                        KOREA_1.equals(ordering) || GB_1.equals(ordering)) {
                    String ucsName = "Adobe-" + ordering + "-" + UCS2;
                    this.ucsCMap = new PDCMap(COSName.construct(ucsName));
                }
            }
        }
    }

    /**
     * Updates font program information from descendant CID font.
     *
     * @param descendant is descendant CID font for this Type 0 font.
     */
    public void setFontProgramFromDescendant(PDCIDFont descendant) {
        this.fontProgram = descendant.fontProgram;
        this.isFontParsed = true;
    }

    /**
     * @return COSDictionary that is font dictionary of this Type 0 font.
     */
    public COSDictionary getType0FontDict() {
        return type0FontDict;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ASAtom getSubtype() {
        return this.type0FontDict.getNameKey(ASAtom.SUBTYPE);
    }

    /**
     * Gets CID value for given character code from this font.
     *
     * @param code is character code.
     * @return CID value for code.
     */
    public int toCID(int code) {
        return this.pdcMap.getCMapFile().toCID(code);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public COSObject getObject() {
        return new COSObject(this.type0FontDict);
    }
}
