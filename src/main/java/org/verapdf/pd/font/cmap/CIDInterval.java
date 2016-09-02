package org.verapdf.pd.font.cmap;

/**
 * This class represents continuous interval of CIDs with increasing values.
 *
 * @author Sergey Shemyakov
 */
class CIDInterval implements CIDMappable {

    private int intervalStart, intervalEnd;
    protected int startingCID;

    CIDInterval(int intervalStart, int intervalEnd, int startingCID) {
        this.intervalStart = intervalStart;
        this.intervalEnd = intervalEnd;
        this.startingCID = startingCID;
    }

    /**
     * Method checks if given character belongs to this particular CID interval.
     *
     * @param character is code of character to be checked.
     * @return true if CID for character can be found in this CID interval.
     */
    public boolean contains(int character) {
        return character >= intervalStart && character <= intervalEnd;
    }

    /**
     * Method returns CID for given character on condition it lies inside this
     * interval.
     *
     * @param character is code of character.
     * @return CID of given character.
     */
    public int getCID(int character) {
        if (!contains(character)) {
            return -1;
        }
        return startingCID + character - intervalStart;
    }
}
