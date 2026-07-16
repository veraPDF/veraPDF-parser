/*
 * This file is part of veraPDF Parser, a module of the veraPDF project.
 * Copyright (c) 2015-2026, veraPDF Consortium <info@verapdf.org>
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
 * Represents W2 array in CID fonts.
 *
 * @author Vladimir Burshnev
 */
public class CIDW2Array {

    private static final Logger LOGGER = Logger.getLogger(CIDW2Array.class.getCanonicalName());

    private final Map<Integer, CIDVerticalMetrics> singleMappings;
    private final List<CIDW2ArrayRange> ranges;

    /**
     * Constructor from a COSObject.
     *
     * @param w2 is W2 array from CIDFont dictionary.
     */
    public CIDW2Array(COSArray w2) {
        singleMappings = new HashMap<>();
        ranges = new ArrayList<>();
        if (w2 != null) {
            for (int i = 0; i < w2.size(); ++i) {
                Long cidBegin = w2.at(i++).getInteger();
                if (cidBegin == null) {
                    LOGGER.log(Level.FINE, "W2 array in CIDFont is invalid.");
                    return;
                }
                COSObject obj = w2.at(i);
                if (obj.getType() == COSObjType.COS_INTEGER) {
                    int cidEnd = obj.getInteger().intValue();
                    CIDVerticalMetrics verticalMetrics = getVerticalMetricsFromCOSArray(w2, ++i);
                    if (verticalMetrics == null) {
                        LOGGER.log(Level.FINE, "W2 array in CIDFont is invalid.");
                        return;
                    }
                    i += 2;
                    this.ranges.add(new CIDW2ArrayRange(cidBegin.intValue(), cidEnd, verticalMetrics));
                } else if (obj.getType() == COSObjType.COS_ARRAY) {
                    addSingleMappings(cidBegin.intValue(), (COSArray) obj.getDirectBase());
                }
            }
        }
    }

    /**
     * Get vertical metrics from W2 array.
     * @param arr is COSArray.
     * @param index is position of displacement vector in COSArray.
     * @return vertical metrics as it is specified in W2 array.
     */
    private CIDVerticalMetrics getVerticalMetricsFromCOSArray(COSArray arr, int index) {
        Double displacement = arr.at(index).getReal();
        if (displacement == null) {
            return null;
        }
        Double positionVectorH = arr.at(++index).getReal();
        if (positionVectorH == null) {
            return null;
        }
        Double positionVectorV = arr.at(++index).getReal();
        if (positionVectorV == null) {
            return null;
        }
        return new CIDVerticalMetrics(displacement, positionVectorH, positionVectorV);
    }

    private void addSingleMappings(int cidBegin, COSArray arr) {
        for (int i = 0; i < arr.size(); i += 3) {
            CIDVerticalMetrics verticalMetrics = getVerticalMetricsFromCOSArray(arr, i);
            if (verticalMetrics == null) {
                LOGGER.log(Level.FINE, "W2 array in CIDFont is invalid.");
                return;
            }
            this.singleMappings.put(cidBegin + i / 3, verticalMetrics);
        }
    }

    /**
     * Get vertical component of the displacement vector of glyph with given cid according to W2 array.
     * @param cid is cid of glyph in CIDFont.
     * @return width as it is specified in W2 array.
     */
    public Double getDisplacement(int cid) {
        CIDVerticalMetrics res = singleMappings.get(cid);
        if (res != null) return res.getDisplacement();
        for (CIDW2ArrayRange range : ranges) {
            if (range.contains(cid)) {
                return range.getDisplacement();
            }
        }
        return null;
    }
}
