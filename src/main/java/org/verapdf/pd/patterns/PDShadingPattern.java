package org.verapdf.pd.patterns;

import org.verapdf.as.ASAtom;
import org.verapdf.cos.COSObjType;
import org.verapdf.cos.COSObject;
import org.verapdf.tools.TypeConverter;

/**
 * @author Maksim Bezrukov
 */
public class PDShadingPattern extends PDPattern {

    public PDShadingPattern(COSObject obj) {
        super(obj);
    }

    @Override
    public int getPatternType() {
        return 1;
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
