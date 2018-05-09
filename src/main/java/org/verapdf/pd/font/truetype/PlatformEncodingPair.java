package org.verapdf.pd.font.truetype;

/**
 * Represents pair of platform and encoding values for cmap table.
 *
 * @author Sergey Shemyakov
 */
class PlatformEncodingPair {

    private int platformID;
    private int encodingID;

    PlatformEncodingPair(int platformID, int encodingID) {
        this.platformID = platformID;
        this.encodingID = encodingID;
    }

    int getPlatformID() {
        return platformID;
    }

    int getEncodingID() {
        return encodingID;
    }
}
