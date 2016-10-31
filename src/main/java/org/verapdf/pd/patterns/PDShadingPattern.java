package org.verapdf.pd.patterns;

import org.verapdf.as.ASAtom;
import org.verapdf.cos.COSObjType;
import org.verapdf.cos.COSObject;
import org.verapdf.pd.PDExtGState;
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
        return PDPattern.TYPE_SHADING_PATTERN;
    }

    public PDShading getShading() {
        COSObject obj = getKey(ASAtom.SHADING);
        if (obj != null && obj.getType().isDictionaryBased()) {
            return new PDShading(obj);
        } else {
            return null;
        }
    }

    public double[] getMatrix() {
        return TypeConverter.getRealArray(getKey(ASAtom.MATRIX), 6, "Matrix");
    }

    public PDExtGState getExtGState() {
        COSObject obj = getKey(ASAtom.EXT_G_STATE);
        if (obj.getType() == COSObjType.COS_DICT) {
            return new PDExtGState(obj);
        }
        return null;
    }
}
