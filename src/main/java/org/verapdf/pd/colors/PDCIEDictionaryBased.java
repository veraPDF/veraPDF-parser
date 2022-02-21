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
package org.verapdf.pd.colors;

import org.verapdf.as.ASAtom;
import org.verapdf.cos.COSDictionary;
import org.verapdf.cos.COSObjType;
import org.verapdf.cos.COSObject;
import org.verapdf.tools.TypeConverter;

import java.awt.color.ColorSpace;

/**
 * @author Maksim Bezrukov
 */
public abstract class PDCIEDictionaryBased extends PDColorSpace {

    private static final double[] DEFAULT_BLACK_POINT = new double[]{0, 0, 0};

    protected COSObject dictionary;

    protected double wpX = 1;
    protected double wpY = 1;
    protected double wpZ = 1;

    protected PDCIEDictionaryBased() {
        this(COSDictionary.construct());
    }

    protected PDCIEDictionaryBased(COSObject obj) {
        super(obj);
        COSObject dict = obj.at(1);
        this.dictionary = (dict == null || !(dict.getType() == COSObjType.COS_DICT)) ?
                COSDictionary.construct()
                : dict;
        fillWhitepointCache(getWhitePoint());
    }

    private void fillWhitepointCache(double[] whitepoint) {
        wpX = whitepoint[0];
        wpY = whitepoint[1];
        wpZ = whitepoint[2];
    }

    protected double[] convXYZtoRGB(double x, double y, double z) {
        float[] rgb = ColorSpace.getInstance(ColorSpace.CS_CIEXYZ)
                .toRGB(new float[]{(float) Math.max(0, x), (float) Math.max(0, y), (float) Math.max(0, z)});
        return new double[]{(double) rgb[0], (double) rgb[1], (double) rgb[2]};
    }

    public double[] getWhitePoint() {
        return getTristimulus(dictionary.getKey(ASAtom.WHITE_POINT));

    }

    public double[] getBlackPoint() {
        double[] res = getTristimulus(dictionary.getKey(ASAtom.BLACK_POINT));
        return res == null ? DEFAULT_BLACK_POINT : res;
    }

    private static double[] getTristimulus(COSObject object) {
        return TypeConverter.getRealArray(object, 3, "Tristimulus");
    }
}
