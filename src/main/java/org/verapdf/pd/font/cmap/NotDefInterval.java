package org.verapdf.pd.font.cmap;

/**
 * Class represents notdef interval. All chars from this interval are mapped
 * into one specified notdef value.
 *
 * @author Sergey Shemyakov
 */
class NotDefInterval extends CIDInterval {

    NotDefInterval(int intervalStart, int intervalEnd, int startingCID) {
        super(intervalStart, intervalEnd, startingCID);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getCID(int character) {
        if(!contains(character)) {
            return -1;
        }
        return startingCID;
    }
}
