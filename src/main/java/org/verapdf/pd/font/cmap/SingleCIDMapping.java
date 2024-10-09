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
package org.verapdf.pd.font.cmap;

/**
 * Class represents single character mapping.
 * @author Sergey Shemyakov
 */
class SingleCIDMapping implements CIDMappable {

    private final int from;
    private final int to;

    SingleCIDMapping(int from, int to) {
        this.from = from;
        this.to = to;
    }

    @Override
    public int getCID(int character) {
        if (character != from) {
            return -1;
        }
        return to;
    }

    @Override
    public int getMaxCID() {
        return to;
    }
}
