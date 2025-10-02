package org.verapdf.pd.optionalcontent;

import org.verapdf.as.ASAtom;
import org.verapdf.cos.*;

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

    public static boolean isVisibleOCMD(COSBase property, PDOptionalContentProperties optProperties) {
        COSObject veProperty = property.getKey(ASAtom.VE);
        if (veProperty != null && veProperty.getType() == COSObjType.COS_ARRAY) {
            COSArray veArray = (COSArray) veProperty.getDirectBase();
            return evaluateVE(veArray, optProperties);
        }

        return isVisibleOCMDByP(property, optProperties);
    }

    private static boolean evaluateVE(COSArray expr, PDOptionalContentProperties optProperties) {
        if (expr.size() == 0) return true;

        COSBase first = expr.at(0).getDirectBase();

        if (first instanceof COSName) {
            ASAtom operator = first.getName();

            if (ASAtom.OR.equals(operator)) {
                for (int i = 1; i < expr.size(); i++) {
                    if (evaluateOperand(expr.at(i).getDirectBase(), optProperties)) {
                        return true;
                    }
                }
                return false;
            } else if (ASAtom.AND.equals(operator)) {
                for (int i = 1; i < expr.size(); i++) {
                    if (!evaluateOperand(expr.at(i).getDirectBase(), optProperties)) {
                        return false;
                    }
                }
                return true;
            } else if (ASAtom.NOT.equals(operator)) {
                if (expr.size() != 2) return true;
                return !evaluateOperand(expr.at(1).getDirectBase(), optProperties);
            }
        }

        for (COSObject obj : expr) {
            if (!evaluateOperand(obj.getDirectBase(), optProperties)) {
                return false;
            }
        }
        return true;
    }


    private static boolean evaluateOperand(COSBase operand, PDOptionalContentProperties optProperties) {
        if (operand instanceof COSArray) {
            return evaluateVE((COSArray) operand, optProperties);

        } else if (operand instanceof COSDictionary) {
            COSDictionary dict = (COSDictionary) operand;
            String name = dict.getStringKey(ASAtom.NAME);
            return optProperties.isVisibleLayer(name);
        }

        return true;
    }
}
