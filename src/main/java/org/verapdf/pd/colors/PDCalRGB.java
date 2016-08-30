package org.verapdf.pd.colors;

import org.verapdf.as.ASAtom;
import org.verapdf.cos.COSObject;
import org.verapdf.tools.TypeConverter;

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
    public ASAtom getType() {
        return ASAtom.CALRGB;
    }

    public double[] getGamma() {
        return TypeConverter.getRealArray(getObject().getKey(ASAtom.GAMMA), 3, "Gamma");
    }

    public double[] getMatrix() {
        return TypeConverter.getRealArray(getObject().getKey(ASAtom.MATRIX), 9, "Matrix");
    }
}
