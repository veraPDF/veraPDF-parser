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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Instance of this class represents CMap subtable of Type 1 font.
 *
 * @author Sergey Shemyakov
 */
public class TrueTypeCmapSubtable {

    private final int platformID;
    private final int encodingID;
    private final long offset;
    private final Map<Integer, Integer> mapping;
    private int sampleCode;

    private static final List<PlatformEncodingPair> standardEncodingCMaps = new ArrayList<>();

    static {
        standardEncodingCMaps.add(new PlatformEncodingPair(3,1));
        standardEncodingCMaps.add(new PlatformEncodingPair(1,0));
    }

    /**
     * Constructor.
     *
     * @param platformID is platform ID of CMap.
     * @param encodingID is encoding ID of CMap.
     * @param offset     is byte offset at which CMap data can be found.
     */
    public TrueTypeCmapSubtable(int platformID, int encodingID, long offset) {
        this.platformID = platformID;
        this.encodingID = encodingID;
        this.offset = offset;
        this.mapping = new HashMap<>();
        this.sampleCode = -1;
    }

    int getPlatformID() {
        return platformID;
    }

    int getEncodingID() {
        return encodingID;
    }

    /**
     * @return byte offset at which CMap data can be found.
     */
    public long getOffset() {
        return offset;
    }

    /**
     * Method adds mapping CID -> GID to CMap.
     *
     * @param key   is CID.
     * @param value is GID.
     */
    public void put(Integer key, Integer value) {
        if (sampleCode == -1) {
            sampleCode = key;
        }
        this.mapping.put(key, value);
    }

    /**
     * Gets GID for specified CID.
     *
     * @param code is character code.
     * @return glyph ID for this character code.
     */
    public int getGlyph(int code) {
        return mapping.getOrDefault(code, 0);
    }

    int getSampleCharCode() {
        return this.sampleCode;
    }

    /**
     * Checks if particular glyph ID is present in this CMap.
     *
     * @param glyphCode is glyph ID.
     * @return true if glyph is present in this CMap.
     */
    public boolean containsGlyph(int glyphCode) {
        return this.mapping.containsValue(glyphCode);
    }

    /**
     * Checks if particular character ID is present in this CMap.
     *
     * @param cid is character ID.
     * @return true if this CID is present.
     */
    public boolean containsCID(int cid) {
        return this.mapping.containsKey(cid);
    }

    public boolean isStandardEncodingCMap() {
        for (PlatformEncodingPair pair : standardEncodingCMaps) {
            if (this.platformID == pair.getPlatformID() &&
                    this.encodingID == pair.getEncodingID()) {
                return true;
            }
        }
        return false;
    }
}
