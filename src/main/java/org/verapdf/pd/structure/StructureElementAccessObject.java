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
        if (mcid != null) {
            COSObject parents = parentTreeRoot.getObject(structParents);
            if (parents != null && !parents.empty() && parents.getType() == COSObjType.COS_ARRAY &&
                    parents.size() > mcid) {
                return parents.at(mcid.intValue());
            }
        }
        return null;
    }

}
