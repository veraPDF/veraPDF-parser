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

import org.verapdf.as.ASAtom;
import org.verapdf.cos.COSName;

import java.awt.color.ColorSpace;

/**
 * @author Maksim Bezrukov
 */
public class PDDeviceRGB extends PDColorSpace {

    public static final PDDeviceRGB INSTANCE = new PDDeviceRGB(false);
    public static final PDDeviceRGB INHERITED_INSTANCE = new PDDeviceRGB(true);

    private final ColorSpace colorSpaceRGB = ColorSpace.getInstance(ColorSpace.CS_sRGB);

    private PDDeviceRGB(boolean isInherited) {
        super(COSName.construct(ASAtom.DEVICERGB));
        setInherited(isInherited);
    }

    @Override
    public int getNumberOfComponents() {
        return 3;
    }

    @Override
    public ASAtom getType() {
        return ASAtom.DEVICERGB;
    }

    @Override
    public double[] toRGB(double[] value) {
        float[] rgb = new float[value.length];
        for (int i = 0; i < value.length; ++i) {
            rgb[i] = (float) value[i];
        }
        rgb = colorSpaceRGB.toRGB(rgb);
        return new double[]{rgb[0], rgb[1], rgb[2]};
    }
}
