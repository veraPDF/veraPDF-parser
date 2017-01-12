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
import org.verapdf.cos.COSDictionary;
import org.verapdf.cos.COSObjType;
import org.verapdf.cos.COSObject;
import org.verapdf.pd.PDContentStream;
import org.verapdf.pd.PDResources;
import org.verapdf.tools.TypeConverter;

/**
 * @author Maksim Bezrukov
 */
public class PDTilingPattern extends PDPattern implements PDContentStream {

    public PDTilingPattern(COSObject obj) {
        super(obj);
    }

    @Override
    public int getPatternType() {
        return PDPattern.TYPE_TILING_PATTERN;
    }

    @Override
    public COSObject getContents() {
        return super.getObject();
    }

    @Override
    public void setContents(COSObject contents) {
        super.setObject(contents);
    }

    public Long getPaintType() {
        return getObject().getIntegerKey(ASAtom.PAINT_TYPE);
    }

    public Long getTilingType() {
        return getObject().getIntegerKey(ASAtom.TILING_TYPE);
    }

    public double[] getBBox() {
        return TypeConverter.getRealArray(getKey(ASAtom.BBOX), 4, "BBox");
    }

    public Double getXStep() {
        return getObject().getRealKey(ASAtom.X_STEP);
    }

    public Double getYStep() {
        return getObject().getRealKey(ASAtom.Y_STEP);
    }

    public double[] getMatrix() {
        return TypeConverter.getRealArray(getKey(ASAtom.MATRIX), 6, "Matrix");
    }

    public PDResources getResources() {
        COSObject resources = getKey(ASAtom.RESOURCES);
        if (resources != null && resources.getType() == COSObjType.COS_DICT) {
            return new PDResources(resources);
        }
        return new PDResources(COSDictionary.construct());
    }
}
