package org.verapdf.pd.colors;

import org.verapdf.cos.COSObject;
import org.verapdf.pd.PDResource;

/**
 * @author Maksim Bezrukov
 */
public abstract class PDColorSpace extends PDResource {

    protected PDColorSpace() {
    }

    protected PDColorSpace(COSObject obj) {
        super(obj);
    }

    public abstract int getNumberOfComponents();
}
