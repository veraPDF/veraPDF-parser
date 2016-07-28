package org.verapdf.pd.patterns;

import org.apache.log4j.Logger;
import org.verapdf.as.ASAtom;
import org.verapdf.cos.COSObjType;
import org.verapdf.cos.COSObject;
import org.verapdf.tools.TypeConverter;

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
        return TypeConverter.getRealArray(getKey(ASAtom.MATRIX), 6, "Matrix");
    }

//    TODO: implement me
//    public PDGraphicsState getExtGState() {
//
//    }
}
