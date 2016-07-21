package org.verapdf.pd.colors;

import org.verapdf.cos.COSObject;
import org.verapdf.pd.PDResources;

/**
 * @author Maksim Bezrukov
 */
public abstract class PDColorSpace extends PDResources {

    protected PDColorSpace() {
    }

    protected PDColorSpace(COSObject obj) {
        super(obj);
    }

    public abstract int getNumberOfComponents();
}
