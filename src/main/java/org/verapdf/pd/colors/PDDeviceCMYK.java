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
import org.verapdf.cos.COSName;

/**
 * @author Maksim Bezrukov
 */
public class PDDeviceCMYK extends PDColorSpace {

    public static final PDDeviceCMYK INSTANCE = new PDDeviceCMYK(false);
    public static final PDDeviceCMYK INHERITED_INSTANCE = new PDDeviceCMYK(true);

    private PDDeviceCMYK(boolean isInherited) {
        super(COSName.construct(ASAtom.DEVICECMYK));
        setInherited(isInherited);
    }

    @Override
    public int getNumberOfComponents() {
        return 4;
    }

    @Override
    public ASAtom getType() {
        return ASAtom.DEVICECMYK;
    }

    @Override
    public double[] toRGB(double[] cmyk) {
        double[] rgb = new double[3];
        rgb[0] = 1.0d - Math.min(1.0, cmyk[0] + cmyk[3]);
        rgb[1] = 1.0d - Math.min(1.0, cmyk[1] + cmyk[3]);
        rgb[2] = 1.0d - Math.min(1.0, cmyk[2] + cmyk[3]);
        return rgb;
    }
}
