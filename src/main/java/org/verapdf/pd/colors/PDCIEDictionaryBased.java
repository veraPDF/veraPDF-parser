package org.verapdf.pd.colors;

import org.verapdf.as.ASAtom;
import org.verapdf.cos.COSDictionary;
import org.verapdf.cos.COSObject;
import org.verapdf.tools.TypeConverter;

/**
 * @author Maksim Bezrukov
 */
public abstract class PDCIEDictionaryBased extends PDColorSpace {

    protected PDCIEDictionaryBased() {
        this(COSDictionary.construct());
    }

    protected PDCIEDictionaryBased(COSObject obj) {
        super(obj);
    }

    public double[] getWhitePoint() {
        return getTristimulus(getObject().getKey(ASAtom.WHITE_POINT));

    }

    public double[] getBlackPoint() {
        return getTristimulus(getObject().getKey(ASAtom.BLACK_POINT));

    }

    private static double[] getTristimulus(COSObject object) {
        return TypeConverter.getRealArray(object, 3, "Tristimulus");
    }
}
