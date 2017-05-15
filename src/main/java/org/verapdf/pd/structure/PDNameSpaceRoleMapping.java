package org.verapdf.pd.structure;

import org.verapdf.as.ASAtom;
import org.verapdf.cos.COSObject;
import org.verapdf.pd.PDObject;

/**
 * @author Sergey Shemyakov
 */
public class PDNameSpaceRoleMapping extends PDObject {

    public PDNameSpaceRoleMapping(COSObject obj) {
        super(obj);
    }

    public StructureType getEquivalentType(ASAtom type) {
        if (knownKey(type)) {
            return StructureType.createStructureType(getKey(type));
        }
        return null;
    }
}
