package org.verapdf.font.truetype;

import java.util.HashMap;
import java.util.Map;

/**
 * Instance of this class represents CMap subtable of Type 1 font.
 *
 * @author Sergey Shemyakov
 */
public class TrueTypeCmapSubtable {

    private int platformID;
    private int encodingID;
    private long offset;
    private Map<Integer, Integer> mapping;
    private int sampleCode;

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
        if (sampleCode != -1) {
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
        if (!mapping.containsKey(code)) {
            return 0;
        } else {
            return mapping.get(code);
        }
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
}
