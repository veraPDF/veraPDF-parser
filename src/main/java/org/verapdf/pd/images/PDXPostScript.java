package org.verapdf.pd.images;

import org.verapdf.as.ASAtom;
import org.verapdf.cos.COSObject;

/**
 * @author Maksim Bezrukov
 */
public class PDXPostScript extends PDXObject {
    protected PDXPostScript(COSObject obj) {
        super(obj);
    }

    @Override
    public ASAtom getType() {
        return ASAtom.PS;
    }
}
