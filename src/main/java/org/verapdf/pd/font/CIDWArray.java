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

import org.verapdf.cos.COSArray;
import org.verapdf.cos.COSObjType;
import org.verapdf.cos.COSObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Represents W array in CID fonts.
 *
 * @author Sergey Shemyakov
 */
public class CIDWArray {

    private static final Logger LOGGER = Logger.getLogger(CIDWArray.class.getCanonicalName());

    private Map<Integer, Double> singleMappings;
    private List<CIDWArrayRange> ranges;

    /**
     * Constructor from a COSObject.
     *
     * @param w is W array from CIDFont dictionary.
     */
    public CIDWArray(COSArray w) {
        singleMappings = new HashMap<>();
        ranges = new ArrayList<>();
        if (w != null) {
            for (int i = 0; i < w.size().intValue(); ++i) {
                int cidBegin = w.at(i++).getInteger().intValue();
                COSObject obj = w.at(i);
                if (obj.getType() == COSObjType.COS_INTEGER) {
                    int cidEnd = obj.getInteger().intValue();
                    Double width = w.at(++i).getReal();
                    if (width == null) {
                        LOGGER.log(Level.FINE, "Unexpected end of W array in CID font");
                        return;
                    }
                    this.ranges.add(new CIDWArrayRange(cidBegin, cidEnd, width.doubleValue()));
                } else if (obj.getType() == COSObjType.COS_ARRAY) {
                    addSingleMappings(cidBegin, (COSArray) obj.getDirectBase());
                }
            }
        }
    }

    private void addSingleMappings(int cidBegin, COSArray arr) {
        for (int i = 0; i < arr.size().intValue(); i++) {
            if (!arr.at(i).getType().isNumber()) {
                LOGGER.log(Level.SEVERE, "W array in CIDFont has invalid entry.");
                continue;
            }
            this.singleMappings.put(Integer.valueOf(cidBegin + i), arr.at(i).getReal());
        }
    }

    /**
     * Get width of glyph with given cid according to W array.
     * @param cid is cid of glyph in CIDFont.
     * @return width as it is specified in W array.
     */
    public Double getWidth(int cid) {
        Double res = singleMappings.get(Integer.valueOf(cid));
        if (res == null) {
            for (CIDWArrayRange range : ranges) {
                if (range.contains(cid)) {
                    res = Double.valueOf(range.getWidth());
                    break;
                }
            }
        }
        return res;
    }

}
