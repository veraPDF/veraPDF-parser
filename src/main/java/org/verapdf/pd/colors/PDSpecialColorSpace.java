/**
 * This file is part of veraPDF Parser, a module of the veraPDF project.
 * Copyright (c) 2015-2025, veraPDF Consortium <info@verapdf.org>
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
package org.verapdf.pd.colors;

import org.verapdf.cos.COSObject;
import org.verapdf.cos.COSReal;
import org.verapdf.factory.colors.ColorSpaceFactory;
import org.verapdf.pd.PDResources;
import org.verapdf.pd.function.PDFunction;

import java.util.ArrayList;
import java.util.List;

/**
 * Base class for special color spaces, see 8.6.6 of PDF-1.7 specification.
 *
 * @author Sergey Shemyakov
 */
public abstract class PDSpecialColorSpace extends PDColorSpace {

    private final PDResources resources;
    private boolean wasDefault;

    /**
     * Constructor from colorspace COSObject and resources.
     */
    public PDSpecialColorSpace(COSObject obj, PDResources resources, boolean wasDefault) {
        super(obj);
        this.resources = resources;
        this.wasDefault = wasDefault;
    }

    protected PDColorSpace getBaseColorSpace() {
        return ColorSpaceFactory.getColorSpace(
                getBaseColorSpaceObject(), this.resources, wasDefault);
    }

    protected double[] getDoubleArrayResult(double[] src, PDFunction function) {
        List<COSObject> values = new ArrayList<>();
        for (double item : src) {
            values.add(COSReal.construct(item));
        }
        List<COSObject> result = function.getResult(values);
        double[] resultValue = new double[result.size()];
        for (int i = 0; i < result.size(); ++i) {
            resultValue[i] = result.get(i).getReal();
        }
        return resultValue;
    }

    public void setWasDefault(boolean wasDefault) {
        this.wasDefault = wasDefault;
    }

    protected PDResources getResources() {
        return resources;
    }

    abstract COSObject getBaseColorSpaceObject();
}
