package org.verapdf.pd.optionalcontent;

import org.verapdf.as.ASAtom;
import org.verapdf.cos.COSArray;
import org.verapdf.cos.COSObject;

import java.util.ArrayList;
import java.util.List;

public class PDOCMDDictionary {
    public static boolean isVisibleOCMDByP(ASAtom pValue, COSObject ocgProperty, PDOptionalContentProperties optProperties) {
        COSArray ocgs = (COSArray) ocgProperty.getDirectBase();
        if (ocgs == null) {
            return true;
        }
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
