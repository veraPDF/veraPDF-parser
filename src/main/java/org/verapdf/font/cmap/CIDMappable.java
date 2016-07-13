package org.verapdf.font.cmap;

/**
 * Interface represents object that makes mapping of character code into CID.
 *
 * @author Sergey Shemyakov
 */
interface CIDMappable {

    /**
     * Gets CID of given character.
     *
     * @param character is code of given character.
     * @return CID of given character or -1 if no mapping available.
     */
    int getCID(int character);
}
