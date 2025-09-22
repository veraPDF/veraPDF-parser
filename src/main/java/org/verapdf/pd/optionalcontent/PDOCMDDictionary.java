package org.verapdf.pd.optionalcontent;

import org.verapdf.as.ASAtom;
import org.verapdf.cos.COSArray;
import org.verapdf.cos.COSBase;
import org.verapdf.cos.COSObjType;
import org.verapdf.cos.COSObject;

public class PDOCMDDictionary {
    public static boolean isVisibleOCMDByP(COSBase property, PDOptionalContentProperties optProperties) {
        COSObject ocgProperty = property.getKey(ASAtom.OCGS);
        if (ocgProperty == null || ocgProperty.getType() != COSObjType.COS_ARRAY) {
            return true;
        }
        COSArray ocgs = (COSArray) ocgProperty.getDirectBase();
        ASAtom pValue = property.getNameKey(ASAtom.P);
        for (COSObject obj : ocgs) {
            boolean isVisible = optProperties.isVisibleLayer(obj.getStringKey(ASAtom.NAME));
            if (isVisible) {
                if (pValue == null || ASAtom.ANY_ON.equals(pValue)) {
                    return true;
                }
                if (ASAtom.ALL_OFF.equals(pValue)) {
                    return false;
                }
            } else {
                if (ASAtom.ALL_ON.equals(pValue)) {
                    return false;
                }
                if (ASAtom.ANY_OFF.equals(pValue)) {
                    return true;
                }
            }
        }
        return ASAtom.ALL_OFF.equals(pValue) || ASAtom.ALL_ON.equals(pValue);
    }
}
