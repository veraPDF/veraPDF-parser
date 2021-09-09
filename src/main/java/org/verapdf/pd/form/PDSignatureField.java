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
package org.verapdf.pd.form;

import org.verapdf.as.ASAtom;
import org.verapdf.cos.*;
import org.verapdf.pd.PDSignature;

import java.util.Set;

/**
 * Represents signature field.
 *
 * @author Sergey Shemyakov
 */
public class PDSignatureField extends PDFormField {

    protected PDSignatureField(COSObject obj, Set<COSKey> parents) {
        super(obj, parents);
    }

    /**
     * @return digital signature contained in this signature field, or null if
     * digital signature can't be obtained.
     */
    public PDSignature getSignature() {
        COSBase directBase = this.getObject().getKey(ASAtom.V).getDirectBase();
        if (directBase != null && directBase.getType() == COSObjType.COS_DICT) {
        	return new PDSignature((COSDictionary) directBase);
        }
        return null;
    }

    /**
     * @return COSObject representing indirect reference to digital signature
     * contained in this signature field.
     */
    public COSObject getSignatureReference() {
        return this.getObject().getKey(ASAtom.V);
    }
}
