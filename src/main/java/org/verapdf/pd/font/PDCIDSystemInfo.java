/**
 * This file is part of veraPDF Parser, a module of the veraPDF project.
 * Copyright (c) 2015-2025, veraPDF Consortium <info@verapdf.org>
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

import org.verapdf.as.ASAtom;
import org.verapdf.cos.COSObject;
import org.verapdf.pd.PDObject;

/**
 * Represents CIDSystemInfo dictionary in CID fonts.
 *
 * @author Sergey Shemyakov
 */
public class PDCIDSystemInfo extends PDObject {

    public PDCIDSystemInfo(COSObject obj) {
        super(obj);
    }

    /**
     * @return a string identifying the issuer of the character collection.
     */
    public String getRegistry() {
        return getStringKey(ASAtom.REGISTRY);
    }

    /**
     * @return a string that uniquely names the character collection within the
     * specified registry.
     */
    public String getOrdering() {
        return getStringKey(ASAtom.ORDERING);
    }

    /**
     * @return the supplement number of the character collection.
     */
    public Long getSupplement() {
        return getIntegerKey(ASAtom.SUPPLEMENT);
    }
}
