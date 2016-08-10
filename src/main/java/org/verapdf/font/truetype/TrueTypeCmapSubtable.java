package org.verapdf.font.truetype;

import java.util.HashMap;
import java.util.Map;

/**
 * This structure contains Platform ID and Encoding ID of Type 1 font.
 *
 * @author Sergey Shemyakov
 */
public class TrueTypeCmapSubtable {

    private int platformID;
    private int encodingID;
    private long offset;
    private Map<Integer, Integer> mapping;
    private int sampleCode;

    public TrueTypeCmapSubtable(int platformID, int encodingID, long offset) {
        this.platformID = platformID;
        this.encodingID = encodingID;
        this.offset = offset;
        this.mapping = new HashMap<>();
        this.sampleCode = -1;
    }

    public int getPlatformID() {
        return platformID;
    }

    public int getEncodingID() {
        return encodingID;
    }

    public long getOffset() {
        return offset;
    }

    public void put(Integer key, Integer value) {
        if (sampleCode != -1) {
            sampleCode = key;
        }
        this.mapping.put(key, value);
    }

    public int getGlyph(int code) {
        if (!mapping.containsKey(code)) {
            return 0;
        } else {
            return mapping.get(code);
        }
    }

    public int getSampleCharCode() {
        return this.sampleCode;
    }
}
