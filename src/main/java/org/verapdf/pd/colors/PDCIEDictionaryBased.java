package org.verapdf.pd.colors;

import org.verapdf.as.ASAtom;
import org.verapdf.cos.COSDictionary;
import org.verapdf.cos.COSObjType;
import org.verapdf.cos.COSObject;
import org.verapdf.tools.TypeConverter;

/**
 * @author Maksim Bezrukov
 */
public abstract class PDCIEDictionaryBased extends PDColorSpace {

    protected COSObject dictionary;

    protected PDCIEDictionaryBased() {
        this(COSDictionary.construct());
    }

    protected PDCIEDictionaryBased(COSObject obj) {
        super(obj);
        COSObject dict = obj.at(1);
        this.dictionary = (dict == null || !(dict.getType() == COSObjType.COS_DICT)) ?
                COSDictionary.construct()
                : dict;
    }

    public double[] getWhitePoint() {
        return getTristimulus(dictionary.getKey(ASAtom.WHITE_POINT));

    }

    public double[] getBlackPoint() {
        return getTristimulus(dictionary.getKey(ASAtom.BLACK_POINT));

    }

    private static double[] getTristimulus(COSObject object) {
        return TypeConverter.getRealArray(object, 3, "Tristimulus");
    }
}
