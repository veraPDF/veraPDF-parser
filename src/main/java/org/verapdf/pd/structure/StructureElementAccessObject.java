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
package org.verapdf.pd.structure;

import org.verapdf.as.ASAtom;
import org.verapdf.cos.COSObjType;
import org.verapdf.cos.COSObject;

/**
 * Class contains methods to access structure elements knowing structParents and
 * MCID or knowing structParent.
 *
 * @author Sergey Shemyakov
 */
public class StructureElementAccessObject {

    private Long structParent;
    private Long structParents;

    public StructureElementAccessObject(COSObject object) {
        if (object != null) {
            this.structParent = object.getIntegerKey(ASAtom.STRUCT_PARENT);
            this.structParents = object.getIntegerKey(ASAtom.STRUCT_PARENTS);
        }
    }

    public COSObject getStructureElement(PDNumberTreeNode parentTreeRoot, Long mcid) {
        if (structParent != null) {
            return parentTreeRoot.getObject(structParent);
        }
        if (mcid != null && structParents != null) {
            COSObject parents = parentTreeRoot.getObject(structParents);
            if (parents != null && !parents.empty() && parents.getType() == COSObjType.COS_ARRAY &&
                    parents.size() > mcid) {
                return parents.at(mcid.intValue());
            }
        }
        return null;
    }

    @Override
    public int hashCode() {
        int result = structParent != null ? structParent.hashCode() : 0;
        result = 31 * result + (structParents != null ? structParents.hashCode() : 0);
        return result;
    }
}
