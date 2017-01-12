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
