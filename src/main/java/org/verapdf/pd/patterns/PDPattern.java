package org.verapdf.pd.patterns;

import org.verapdf.as.ASAtom;
import org.verapdf.cos.COSObject;
import org.verapdf.pd.colors.PDColorSpace;

/**
 * @author Maksim Bezrukov
 */
public abstract class PDPattern extends PDColorSpace {

    protected PDPattern(COSObject obj) {
        super(obj);
    }

    @Override
    public int getNumberOfComponents() {
        return -1;
    }

    @Override
    public ASAtom getType() {
        return ASAtom.PATTERN;
    }

    public abstract int getPatternType();
}
