package org.verapdf.font.cmap;

/**
 * Class represents notdef interval. All chars from this interval are mapped
 * into one specified notdef value.
 *
 * @author Sergey Shemyakov
 */
class NotDefInterval extends CIDInterval {

    public NotDefInterval(int intervalStart, int intervalEnd, int startingCID) {
        super(intervalStart, intervalEnd, startingCID);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getCID(int character) {
        return startingCID;
    }
}
