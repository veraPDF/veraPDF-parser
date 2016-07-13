package org.verapdf.font.cmap;

/**
 * Class represents single character mapping.
 * @author Sergey Shemyakov
 */
class SingleCIDMapping implements CIDMappable {

    private int from, to;

    SingleCIDMapping(int from, int to) {
        this.from = from;
        this.to = to;
    }

    @Override
    public int getCID(int character) {
        if(character != from) {
            return -1;
        }
        return to;
    }
}
