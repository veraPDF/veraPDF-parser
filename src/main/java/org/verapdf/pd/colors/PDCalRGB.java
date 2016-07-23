package org.verapdf.pd.colors;

import org.verapdf.as.ASAtom;
import org.verapdf.cos.COSObject;

/**
 * @author Maksim Bezrukov
 */
public class PDCalRGB extends PDCIEDictionaryBased {

    public PDCalRGB() {
    }

    public PDCalRGB(COSObject obj) {
        super(obj);
    }

    @Override
    public int getNumberOfComponents() {
        return 3;
    }

    @Override
    public ASAtom getName() {
        return ASAtom.CALRGB;
    }

    public double[] getGamma() {
        return getRealArray(getObject().getKey(ASAtom.GAMMA), 3, "Gamma");
    }

    public double[] getMatrix() {
        return getRealArray(getObject().getKey(ASAtom.MATRIX), 9, "Matrix");
    }
}
