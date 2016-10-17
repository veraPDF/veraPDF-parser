package org.verapdf.pd.encryption;

import org.verapdf.as.ASAtom;
import org.verapdf.cos.COSObjType;
import org.verapdf.cos.COSObject;
import org.verapdf.pd.PDObject;

/**
 * Represents crypt filter dictionary on pd level.
 *
 * @author Sergey Shemyakov
 */
public class PDCryptFilter extends PDObject{

    public PDCryptFilter(COSObject obj) {
        super(obj);
    }

    /**
     * @return method used by crypt filter: None, V2 or AESV2.
     */
    public ASAtom getMethod() {
        COSObject obj = getKey(ASAtom.CFM);
        if(obj != null && obj.getType() == COSObjType.COS_NAME) {
            return obj.getName();
        }
        return ASAtom.NONE;
    }

    /**
     * @return length of encryption key specified in crypt filter dictionary.
     */
    public Long getLength() {
        return getIntegerKey(ASAtom.LENGTH);
    }

}
