package org.verapdf.pd;

import org.verapdf.cos.COSObject;

/**
 * @author Maksim Bezrukov
 */
public class PDResources extends PDObject {

    private boolean isInherited = false;

    public PDResources() {
    }

    protected PDResources(COSObject obj) {
        super(obj);
    }

    public boolean isInherited() {
        return isInherited;
    }

    public void setInherited(boolean inherited) {
        isInherited = inherited;
    }
}
