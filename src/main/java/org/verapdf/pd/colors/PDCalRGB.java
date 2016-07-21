package org.verapdf.pd.colors;

import org.verapdf.as.ASAtom;
import org.verapdf.cos.COSObjType;
import org.verapdf.cos.COSObject;

/**
 * @author Maksim Bezrukov
 */
public class PDCalRGB extends PDCIEDictionaryBased {

    public PDCalRGB(COSObject obj) {
        super(obj);
    }

    @Override
    public int getNumberOfComponents() {
        return 3;
    }

    public PDGamma getGamma() {
        COSObject gamma = getObject().getKey(ASAtom.GAMMA);
        if (gamma != null && gamma.getType() == COSObjType.COSArrayT) {
            return new PDGamma(gamma);
        }
        return null;
    }

    public double[] getMatrix() {
        COSObject matrixObject = getObject().getKey(ASAtom.MATRIX);
        if (matrixObject != null && matrixObject.getType() == COSObjType.COSArrayT) {
            int size = matrixObject.size();
            double[] res = new double[size];
            for (int i = 0; i < size; ++i) {

            }
        }
        return null;
    }
}
