package org.verapdf.pd.colors;

import org.verapdf.as.ASAtom;
import org.verapdf.cos.COSObjType;
import org.verapdf.cos.COSObject;

/**
 * @author Maksim Bezrukov
 */
public abstract class PDCIEDictionaryBased extends PDColorSpace {

    protected PDCIEDictionaryBased(COSObject obj) {
        super(obj);
    }

    public PDTristimulus getWhitePoint() {
        return getTristimulus(getObject().getKey(ASAtom.WHITE_POINT));

    }

    public PDTristimulus getBlackPoint() {
        return getTristimulus(getObject().getKey(ASAtom.BLACK_POINT));

    }

    private static PDTristimulus getTristimulus(COSObject object) {
        if (object != null && object.getType() == COSObjType.COS_ARRAY) {
            return new PDTristimulus(object);
        }
        return null;
    }
}
