package org.verapdf.pd.structure;

import org.verapdf.as.ASAtom;
import org.verapdf.cos.COSDictionary;
import org.verapdf.cos.COSObjType;
import org.verapdf.cos.COSObject;
import org.verapdf.pd.PDObject;

/**
 *
 * Represents namespace in structure tree, as described in PDF-2.0 specification
 * 14.7.4.
 *
 * @author Sergey Shemyakov
 */
public class PDStructureElementNameSpace extends PDObject {

    private PDStructureElementNameSpace(COSObject obj) {
        super(obj);
    }

    /**
     * @return the string defining the namespace name.
     */
    public String getNS() {
        COSObject obj = this.getKey(ASAtom.NS);
        if (obj != null && obj.getType() == COSObjType.COS_STRING) {
            return obj.getString();
        }
        return null;
    }

    public COSDictionary getRoleMap() {
        COSObject obj = this.getKey(ASAtom.ROLE_MAP_NS);
        if (obj != null && obj.getType() == COSObjType.COS_DICT) {
            return (COSDictionary) obj.get();
        }
        return null;
    }

    public NameSpaceRoleMapping getNameSpaceMapping() {
        return new NameSpaceRoleMapping(getRoleMap());
    }

    public static PDStructureElementNameSpace getNameSpace(COSObject obj) {
        return getNameSpace(obj, true);
    }

    /**
     * Returns PDStructureElementNameSpace object for given COSObject.
     *
     * @param obj COSObject of name space.
     * @param checkCache true if name space cache should be checked.
     * @return PD object for name space.
     */
    public static PDStructureElementNameSpace getNameSpace(COSObject obj, boolean checkCache) {
        if (!checkCache) {
            return new PDStructureElementNameSpace(obj);
        }
        return NameSpaceFactory.getNameSpace(obj);
    }
}