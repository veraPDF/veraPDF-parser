package org.verapdf.pd.colors;

import org.apache.log4j.Logger;
import org.verapdf.as.ASAtom;
import org.verapdf.cos.COSDictionary;
import org.verapdf.cos.COSObjType;
import org.verapdf.cos.COSObject;

/**
 * @author Maksim Bezrukov
 */
public abstract class PDCIEDictionaryBased extends PDColorSpace {

    private static final Logger LOGGER = Logger.getLogger(PDCIEDictionaryBased.class);

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
        return getRealArray(object, 3, "Tristimulus");
    }

    protected static double[] getRealArray(COSObject array, int estimatedSize, String arrayName) {
        if (arrayName == null) {
            throw new IllegalArgumentException("Array name can not be null");
        }

        if (array != null && array.getType() == COSObjType.COS_ARRAY) {
            int size = array.size();

            if (size != estimatedSize) {
                LOGGER.debug(arrayName + " array doesn't consist of " + estimatedSize + " elements");
            }

            double[] res = new double[size];
            for (int i = 0; i < size; ++i) {
                COSObject number = array.at(i);
                if (number == null || number.getReal() == null) {
                    LOGGER.debug(arrayName + " array contains non number value");
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
