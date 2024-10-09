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
package org.verapdf.pd.encryption;

import org.verapdf.as.ASAtom;
import org.verapdf.cos.COSObjType;
import org.verapdf.cos.COSObject;
import org.verapdf.pd.PDObject;

/**
 * Represents crypt filter dictionary on pd level.
 *
 * @author Sergey Shemyakov
 */
public class PDCryptFilter extends PDObject{

    public PDCryptFilter(COSObject obj) {
        super(obj);
    }

    /**
     * @return method used by crypt filter: None, V2 or AESV2.
     */
    public ASAtom getMethod() {
        COSObject obj = getKey(ASAtom.CFM);
        if (obj != null && obj.getType() == COSObjType.COS_NAME) {
            return obj.getName();
        }
        return ASAtom.NONE;
    }

    /**
     * @return length of encryption key specified in crypt filter dictionary.
     */
    public Long getLength() {
        return getIntegerKey(ASAtom.LENGTH);
    }

}
