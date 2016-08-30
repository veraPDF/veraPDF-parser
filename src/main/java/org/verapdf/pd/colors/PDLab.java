package org.verapdf.pd.colors;

import org.verapdf.as.ASAtom;
import org.verapdf.cos.COSObject;
import org.verapdf.tools.TypeConverter;

/**
 * @author Maksim Bezrukov
 */
public class PDLab extends PDCIEDictionaryBased {

    public PDLab() {
    }

    public PDLab(COSObject obj) {
        super(obj);
    }

    @Override
    public int getNumberOfComponents() {
        return 3;
    }

    @Override
    public ASAtom getType() {
        return ASAtom.LAB;
    }

    public double[] getRange() {
        return TypeConverter.getRealArray(getObject().getKey(ASAtom.RANGE), 4, "Range");
    }
}
