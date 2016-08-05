package org.verapdf.pd;

import org.verapdf.as.ASAtom;
import org.verapdf.cos.COSObjType;
import org.verapdf.cos.COSObject;

/**
 * @author Maksim Bezrukov
 */
public class PDHalftone extends PDObject {

    /**
     * Constructing Halftone object from base object
     * @param obj base object for halftone. Can be name, dictionary or stream
     */
    public PDHalftone(COSObject obj) {
        super(obj);
    }

    public Long getHalftoneType() {
        COSObject base = getObject();
        if (base.getType() == COSObjType.COS_NAME) {
            return null;
        }
        return base.getIntegerKey(ASAtom.HALFTONE_TYPE);
    }

    public String getHalftoneName() {
        COSObject base = getObject();
        if (base.getType() == COSObjType.COS_NAME) {
            return base.getName().getValue();
        }
        return base.getStringKey(ASAtom.HALFTONE_NAME);
    }
}
