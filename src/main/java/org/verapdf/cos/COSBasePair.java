/**
 * This file is part of veraPDF Parser, a module of the veraPDF project.
 * Copyright (c) 2015-2024, veraPDF Consortium <info@verapdf.org>
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
package org.verapdf.cos;

import java.util.List;

/**
 * @author Sergey Shemyakov
 */
public class COSBasePair {
    private final COSBase first;
    private final COSBase second;

    private COSBasePair(COSBase first, COSBase second) {
        this.first = first;
        this.second = second;
    }

    COSBase getFirst() {
        return first;
    }

    COSBase getSecond() {
        return second;
    }

    private boolean contains(COSBase obj) {
        return first == obj || second == obj;
    }

    static boolean listContainsPair(List<COSBasePair> list, COSBase obj1, COSBase obj2) {
        for (COSBasePair pair : list) {
            if (pair.contains(obj1) && pair.contains(obj2)) {
                return true;
            }
        }
        return false;
    }

    static void addPairToList(List<COSBasePair> list, COSBase obj1, COSBase obj2) {
        if (obj1 instanceof COSArray || obj1 instanceof COSDictionary) {
            list.add(new COSBasePair(obj1, obj2));
        }
    }
}
