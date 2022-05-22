/**
 * This file is part of veraPDF Parser, a module of the veraPDF project.
 * Copyright (c) 2015, veraPDF Consortium <info@verapdf.org>
 * All rights reserved.
 *
 * veraPDF Parser is free software: you can redistribute it and/or modify
 * it under the terms of either:
 *
 * The GNU General public license GPLv3+.
 * You should have received a copy of the GNU General Public License
 * along with veraPDF Parser as the LICENSE.GPL file in the root of the source
 * tree.  If not, see http://www.gnu.org/licenses/ or
 * https://www.gnu.org/licenses/gpl-3.0.en.html.
 *
 * The Mozilla Public License MPLv2+.
 * You should have received a copy of the Mozilla Public License along with
 * veraPDF Parser as the LICENSE.MPL file in the root of the source tree.
 * If a copy of the MPL was not distributed with this file, you can obtain one at
 * http://mozilla.org/MPL/2.0/.
 */
package org.verapdf.pd.font.cmap;

/**
 * This class represents continuous interval of CIDs with increasing values.
 *
 * @author Sergey Shemyakov
 */
class CIDInterval implements CIDMappable {

    private int intervalStart;
    private int intervalEnd;
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

    @Override
    public int getMaxCID() {
        return startingCID + intervalEnd - intervalStart;
    }
}
