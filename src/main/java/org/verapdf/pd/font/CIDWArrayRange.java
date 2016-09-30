package org.verapdf.pd.font;

/**
 * Represents range of sequential CIDs and width for them. This is used in W
 * array in CIDFonts.
 *
 * @author Sergey Shemyakov
 */
public class CIDWArrayRange {
    private double width;
    private int beginCID;
    private int endCID;

    public CIDWArrayRange(int beginCID, int endCID, double width) {
        this.width = width;
        this.beginCID = beginCID;
        this.endCID = endCID;
    }

    /**
     * Returns true if width for given CID is stored in this CIDWArrayRange.
     *
     * @param cid is CID to check.
     * @return true if width for this CID can be obtained from this
     * CIDWArrayRange.
     */
    public boolean contains(int cid) {
        return cid >= beginCID && cid <= endCID;
    }

    /**
     * @return width for this range.
     */
    public double getWidth() {
        return width;
    }
}
