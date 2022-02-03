/**
 * This file is part of veraPDF Parser, a module of the veraPDF project.
 * Copyright (c) 2015, veraPDF Consortium <info@verapdf.org>
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
package org.verapdf.pd.function;

import org.verapdf.as.ASAtom;
import org.verapdf.cos.COSArray;
import org.verapdf.cos.COSObject;
import org.verapdf.pd.PDObject;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PDFunction extends PDObject {

    private static final Logger LOGGER = Logger.getLogger(PDFunction.class.getCanonicalName());

    protected PDFunction(COSObject obj) {
       super(obj);
   }

   public static PDFunction createFunction(COSObject obj) {
        if (obj == null || !obj.getType().isDictionaryBased()) {
            return null;
        }

       Long functionType = obj.getIntegerKey(ASAtom.FUNCTION_TYPE);

        if (functionType == null) {
            LOGGER.log(Level.WARNING,"FunctionType is missing or not a number");
            return new PDFunction(obj);
        }

        switch (functionType.intValue()) {
            case 3:
                return new PDType3Function(obj);
            case 4:
                return new PDType4Function(obj);
            default:
                return new PDFunction(obj);
        }
   }

    public Long getFunctionType() {
        return getObject().getIntegerKey(ASAtom.FUNCTION_TYPE);
    }

    public COSArray getCOSArray(final ASAtom key){
        COSObject obj = this.getKey(key);
        return obj == null ? null : (COSArray) obj.getDirectBase();
    }

    public COSArray getDomain(){
        return getCOSArray(ASAtom.DOMAIN);
    }

    public COSArray getRange(){
        return getCOSArray(ASAtom.RANGE);
    }

    public List<COSObject> getValuesInIntervals(List<COSObject> values, COSArray intervals) {
        if (intervals != null && intervals.size() >= values.size() * 2) {
            List<COSObject> result = new ArrayList<>();
            for (int i = 0; i < values.size(); ++i) {
                result.add(min(max(values.get(i), intervals.at(2 * i)), intervals.at(2 * i + 1)));
            }
            return result;
        } else {
            LOGGER.log(Level.WARNING, "Intervals size is invalid");
            return values;
        }
    }

    private COSObject max(COSObject first, COSObject second) {
        return first.getReal() >= second.getReal() ? first : second;
    }

    private COSObject min(COSObject first, COSObject second) {
        return first.getReal() <= second.getReal() ? first : second;
    }
}
