package org.verapdf.pd.colors;

import org.apache.log4j.Logger;
import org.verapdf.as.ASAtom;
import org.verapdf.cos.COSObjType;
import org.verapdf.cos.COSObject;

/**
 * @author Maksim Bezrukov
 */
public class PDLab extends PDCIEDictionaryBased {

    private static final Logger LOGGER = Logger.getLogger(PDLab.class);

    public PDLab(COSObject obj) {
        super(obj);
    }

    @Override
    public int getNumberOfComponents() {
        return 3;
    }

    public double[] getRange() {
        COSObject rangeObject = getObject().getKey(ASAtom.RANGE);
        if (rangeObject != null && rangeObject.getType() == COSObjType.COS_ARRAY) {
            int size = rangeObject.size();

            if (size != 4) {
                LOGGER.debug("Range array doesn't consist of four elements");
            }

            double[] res = new double[size];
            for (int i = 0; i < size; ++i) {
                COSObject number = rangeObject.at(i);
                if (number == null || number.getReal() == null) {
                    LOGGER.debug("Range array contains non number value");
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
