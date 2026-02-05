/*
 * This file is part of veraPDF Parser, a module of the veraPDF project.
 * Copyright (c) 2015-2026, veraPDF Consortium <info@verapdf.org>
 * All rights reserved.
 *
 * veraPDF Parser is free software: you can redistribute it and/or modify
 * it under the terms of either:
 *
 * The GNU General public license GPLv3+.
 * You should have received a copy of the GNU General Public License
 * along with veraPDF Parser as the LICENSE.GPL file in the root of the source
 * tree.  If not, see http://www.gnu.org/licenses/ or
 * https://www.gnu.org/licenses/gpl-3.0.en.html.
 *
 * The Mozilla Public License MPLv2+.
 * You should have received a copy of the Mozilla Public License along with
 * veraPDF Parser as the LICENSE.MPL file in the root of the source tree.
 * If a copy of the MPL was not distributed with this file, you can obtain one at
 * http://mozilla.org/MPL/2.0/.
 */
package org.verapdf.pd.optionalcontent;

import org.verapdf.as.ASAtom;
import org.verapdf.cos.*;

import java.util.logging.Level;
import java.util.logging.Logger;

public class PDOCMDDictionary {
    private static final Logger LOGGER = Logger.getLogger(PDOCMDDictionary.class.getCanonicalName());

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
        if (expr.size() == 0) {
            return true;
        }

        COSBase first = expr.at(0).getDirectBase();

        if (first instanceof COSName) {
            ASAtom operator = first.getName();

            if (ASAtom.OR.equals(operator)) {
                for (int i = 1; i < expr.size(); i++) {
                    if (evaluateOperandOfVE(expr.at(i).getDirectBase(), optProperties)) {
                        return true;
                    }
                }
                return false;
            } else if (ASAtom.AND.equals(operator)) {
                for (int i = 1; i < expr.size(); i++) {
                    if (!evaluateOperandOfVE(expr.at(i).getDirectBase(), optProperties)) {
                        return false;
                    }
                }
                return true;
            } else if (ASAtom.NOT.equals(operator)) {
                if (expr.size() != 2) {
                    LOGGER.log(Level.WARNING, "/Not operator should have only 1 argument in VE array");
                    return true;
                }
                return !evaluateOperandOfVE(expr.at(1).getDirectBase(), optProperties);
            }
            LOGGER.log(Level.WARNING, String.format("First element of VE array has value %s instead of /And, /Or or /Not", operator));
        } else {
            LOGGER.log(Level.WARNING, "First element of VE array should have type name");
        }

        return true;
    }


    private static boolean evaluateOperandOfVE(COSBase operand, PDOptionalContentProperties optProperties) {
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
