package org.verapdf.pd.font.truetype;

/**
 * @author Sergey Shemyakov
 */
public class PlatformEncodingPair {

    private int platformID;
    private int encodingID;

    public PlatformEncodingPair(int platformID, int encodingID) {
        this.platformID = platformID;
        this.encodingID = encodingID;
    }

    public int getPlatformID() {
        return platformID;
    }

    public int getEncodingID() {
        return encodingID;
    }
}
