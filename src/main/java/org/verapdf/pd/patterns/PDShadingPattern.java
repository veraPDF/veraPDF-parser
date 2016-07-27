package org.verapdf.pd.patterns;

import org.apache.log4j.Logger;
import org.verapdf.as.ASAtom;
import org.verapdf.cos.COSObjType;
import org.verapdf.cos.COSObject;

/**
 * @author Maksim Bezrukov
 */
public class PDShadingPattern extends PDPattern {

    private static final Logger LOGGER = Logger.getLogger(PDShadingPattern.class);

    public PDShadingPattern(COSObject obj) {
        super(obj);
    }

    public PDShading getShading() {
        COSObject obj = getKey(ASAtom.SHADING);
        if (obj != null &&
                (obj.getType() == COSObjType.COS_DICT || obj.getType() == COSObjType.COS_STREAM)) {
            return new PDShading(obj);
        } else {
            return null;
        }
    }

    public double[] getMatrix() {
        COSObject bboxObject = getObject().getKey(ASAtom.MATRIX);
        if (bboxObject != null && bboxObject.getType() == COSObjType.COS_ARRAY) {
            int size = bboxObject.size();
            if (size != 6) {
                LOGGER.debug("Matrix array doesn't consist of 6 elements");
            }
            double[] res = new double[size];
            for (int i = 0; i < size; ++i) {
                COSObject number = bboxObject.at(i);
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

//    TODO: implement me
//    public PDGraphicsState getExtGState() {
//
//    }
}
