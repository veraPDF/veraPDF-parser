package org.verapdf.pd.colors;

import org.verapdf.as.ASAtom;
import org.verapdf.cos.COSObject;

/**
 * @author Maksim Bezrukov
 */
public class PDCalGray extends PDCIEDictionaryBased {

    public PDCalGray() {
    }

    public PDCalGray(COSObject obj) {
        super(obj);
    }

    @Override
    public int getNumberOfComponents() {
        return 1;
    }

    @Override
    public ASAtom getName() {
        return ASAtom.CALGRAY;
    }

    public Double getGamma() {
        return getNumber(getObject().getKey(ASAtom.GAMMA));
    }

    private static Double getNumber(COSObject object) {
        if (object != null) {
            return object.getReal();
        }
        return null;
    }
}
