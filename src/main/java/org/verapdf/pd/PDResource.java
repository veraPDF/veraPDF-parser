package org.verapdf.pd;

import org.verapdf.cos.COSObject;

/**
 * @author Maksim Bezrukov
 */
public class PDResource extends PDObject {

    private boolean isInherited = false;

    public PDResource() {
    }

    protected PDResource(COSObject obj) {
        super(obj);
    }

    public boolean isInherited() {
        return isInherited;
    }

    public void setInherited(boolean inherited) {
        isInherited = inherited;
    }
}
