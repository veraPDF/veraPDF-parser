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
package org.verapdf.pd.patterns;

import org.verapdf.as.ASAtom;
import org.verapdf.cos.COSObjType;
import org.verapdf.cos.COSObject;
import org.verapdf.pd.PDExtGState;
import org.verapdf.pd.PDResources;
import org.verapdf.tools.TypeConverter;

/**
 * @author Maksim Bezrukov
 */
public class PDShadingPattern extends PDPattern {

    private final PDResources resources;

    public PDShadingPattern(COSObject obj, PDResources resources) {
        super(obj);
        this.resources = resources;
    }

    @Override
    public int getPatternType() {
        return PDPattern.TYPE_SHADING_PATTERN;
    }

    public PDShading getShading() {
        COSObject obj = getKey(ASAtom.SHADING);
        if (obj != null && obj.getType().isDictionaryBased()) {
            return new PDShading(obj, this.resources);
        } else {
            return null;
        }
    }

    public double[] getMatrix() {
        double[] res = TypeConverter.getRealArray(getKey(ASAtom.MATRIX), 6, "Matrix");
        return res != null ? res : new double[]{1, 0, 0, 1, 0, 0};
    }

    public PDExtGState getExtGState() {
        COSObject obj = getKey(ASAtom.EXT_G_STATE);
        if (obj.getType() == COSObjType.COS_DICT) {
            return new PDExtGState(obj);
        }
        return null;
    }
}
