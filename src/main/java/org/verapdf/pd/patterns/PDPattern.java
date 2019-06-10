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
package org.verapdf.pd.patterns;

import org.verapdf.as.ASAtom;
import org.verapdf.cos.COSName;
import org.verapdf.cos.COSObject;
import org.verapdf.factory.colors.ColorSpaceFactory;
import org.verapdf.pd.PDResources;
import org.verapdf.pd.colors.PDColorSpace;

/**
 * @author Maksim Bezrukov
 */
public class PDPattern extends PDColorSpace {

    public static final PDPattern INSTANCE = new PDPattern(COSName.construct(ASAtom.PATTERN));

    public static final int TYPE_PATTERN = 0;
    public static final int TYPE_TILING_PATTERN = 1;
    public static final int TYPE_SHADING_PATTERN = 2;

    private PDColorSpace underlyingColorSpace = null;

    protected PDPattern(COSObject obj) {
        super(obj);
    }

    private PDPattern(COSObject obj, PDColorSpace underlyingColorSpace) {
        super(obj);
        this.underlyingColorSpace = underlyingColorSpace;
    }

    public static PDPattern createPattern(COSObject underlyingColorSpace, PDResources resources) {
        if (underlyingColorSpace == null || underlyingColorSpace.empty()) {
            return PDPattern.INSTANCE;
        }
        return new PDPattern(COSName.construct(ASAtom.PATTERN), ColorSpaceFactory.getColorSpace(underlyingColorSpace, resources));
    }

    @Override
    public int getNumberOfComponents() {
        return -1;
    }

    @Override
    public ASAtom getType() {
        return ASAtom.PATTERN;
    }

    public int getPatternType() {
        return TYPE_PATTERN;
    }

    public PDColorSpace getUnderlyingColorSpace() {
        return underlyingColorSpace;
    }
}
