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
public class PDCalRGB extends PDCIEDictionaryBased {

    public PDCalRGB() {
    }

    public PDCalRGB(COSObject obj) {
        super(obj);
    }

    @Override
    public int getNumberOfComponents() {
        return 3;
    }

    @Override
    public ASAtom getType() {
        return ASAtom.CALRGB;
    }

    // See ISO 32000-2:2020, chapter 8.6.5.3
    @Override
    public double[] toRGB(double[] value) {
        if (wpX == 1 && wpY == 1 && wpZ == 1) {
            double a = value[0];
            double b = value[1];
            double c = value[2];

            double[] gamma = getGamma();
            double powAR = Math.pow(a, gamma[0]);
            double powBG = Math.pow(b, gamma[1]);
            double powCB = Math.pow(c, gamma[2]);

            double[] matrix = getMatrix();
            double mXA = matrix[0];
            double mYA = matrix[1];
            double mZA = matrix[2];
            double mXB = matrix[3];
            double mYB = matrix[4];
            double mZB = matrix[5];
            double mXC = matrix[6];
            double mYC = matrix[7];
            double mZC = matrix[8];

            double x = mXA * powAR + mXB * powBG + mXC * powCB;
            double y = mYA * powAR + mYB * powBG + mYC * powCB;
            double z = mZA * powAR + mZB * powBG + mZC * powCB;
            return convXYZtoRGB(x, y, z);
        } else {
            return new double[]{value[0], value[1], value[2]};
        }
    }

    public double[] getGamma() {
        double[] res = TypeConverter.getRealArray(this.dictionary.getKey(ASAtom.GAMMA), 3, "Gamma");
        return res != null ? res : new double[]{1, 1, 1};
    }

    public double[] getMatrix() {
        double[] res = TypeConverter.getRealArray(this.dictionary.getKey(ASAtom.MATRIX), 9, "Matrix");
        return res != null ? res : new double[]{1, 0, 0,  0, 1, 0, 0, 0, 1};
    }
}
