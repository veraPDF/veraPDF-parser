package org.verapdf.pd.colors;

import org.apache.log4j.Logger;
import org.verapdf.as.ASAtom;
import org.verapdf.cos.COSObjType;
import org.verapdf.cos.COSObject;

/**
 * @author Maksim Bezrukov
 */
public class PDCalRGB extends PDCIEDictionaryBased {

    private static final Logger LOGGER = Logger.getLogger(PDCalRGB.class);

    public PDCalRGB(COSObject obj) {
        super(obj);
    }

    @Override
    public int getNumberOfComponents() {
        return 3;
    }

    public PDGamma getGamma() {
        COSObject gamma = getObject().getKey(ASAtom.GAMMA);
        if (gamma != null && gamma.getType() == COSObjType.COS_ARRAY) {
            return new PDGamma(gamma);
        }
        return null;
    }

    public double[] getMatrix() {
        COSObject matrixObject = getObject().getKey(ASAtom.MATRIX);
        if (matrixObject != null && matrixObject.getType() == COSObjType.COS_ARRAY) {
            int size = matrixObject.size();

            if (size != 9) {
                LOGGER.debug("Matrix array doesn't consist of nine elements");
            }

            double[] res = new double[size];
            for (int i = 0; i < size; ++i) {
                COSObject number = matrixObject.at(i);
                if (number == null || number.getReal() == null) {
                    LOGGER.debug("Matrix array contains non number value");
                    return null;
                } else {
                    res[i] = number.getReal();
                }
            }
            return res;
        }
        return null;
    }
}
