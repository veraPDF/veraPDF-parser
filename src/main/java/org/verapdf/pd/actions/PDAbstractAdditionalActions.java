package org.verapdf.pd.actions;

import org.verapdf.as.ASAtom;
import org.verapdf.cos.COSObjType;
import org.verapdf.cos.COSObject;
import org.verapdf.pd.PDObject;

/**
 * @author Maksim Bezrukov
 */
public abstract class PDAbstractAdditionalActions extends PDObject {

    protected PDAbstractAdditionalActions(COSObject obj) {
        super(obj);
    }

    protected PDAction getAction(ASAtom key) {
        COSObject obj = getKey(key);
        if (obj != null && obj.getType() == COSObjType.COS_DICT) {
            return new PDAction(obj);
        }
        return null;
    }
}
