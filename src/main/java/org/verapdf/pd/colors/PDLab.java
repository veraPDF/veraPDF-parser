/**
 * This file is part of veraPDF Parser, a module of the veraPDF project.
 * Copyright (c) 2015-2024, veraPDF Consortium <info@verapdf.org>
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

import org.verapdf.as.ASAtom;
import org.verapdf.cos.COSObject;
import org.verapdf.tools.TypeConverter;

/**
 * @author Maksim Bezrukov
 */
public class PDLab extends PDCIEDictionaryBased {

    public PDLab() {
    }

    public PDLab(COSObject obj) {
        super(obj);
    }

    public PDLab(double[] whitepoint) {
        fillWhitepointCache(whitepoint);
    }

    @Override
    public int getNumberOfComponents() {
        return 3;
    }

    @Override
    public ASAtom getType() {
        return ASAtom.LAB;
    }

    // See ISO 32000-2:2020, chapter 8.6.5.4
    @Override
    public double[] toRGB(double[] value) {
        double lstar = (value[0] + 16d) * (1d / 116d);
        double x = wpX * inverse(lstar + value[1] * (1d / 500d));
        double y = wpY * inverse(lstar);
        double z = wpZ * inverse(lstar - value[2] * (1d / 200d));
        return convXYZtoRGB(x, y, z);
    }

    public double[] getRange() {
        double[] res = TypeConverter.getRealArray(this.dictionary.getKey(ASAtom.RANGE), 4, "Range");
        return res != null ? res : new double[]{-100, 100, -100, 100};
    }

    private double inverse(double x) {
        if (x > 6.0 / 29.0) {
            return x * x * x;
        } else {
            return (108d / 841d) * (x - (4d / 29d));
        }
    }
}
