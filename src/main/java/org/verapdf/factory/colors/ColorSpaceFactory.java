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
package org.verapdf.factory.colors;

import org.verapdf.as.ASAtom;
import org.verapdf.cos.COSObjType;
import org.verapdf.cos.COSObject;
import org.verapdf.pd.PDResources;
import org.verapdf.pd.colors.*;
import org.verapdf.pd.patterns.PDPattern;
import org.verapdf.pd.patterns.PDShadingPattern;
import org.verapdf.pd.patterns.PDTilingPattern;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Maksim Bezrukov
 */
public class ColorSpaceFactory {

    private static final Logger LOGGER = Logger.getLogger(ColorSpaceFactory.class.getCanonicalName());

    private ColorSpaceFactory() {
    }

    public static ASAtom getDefaultValue(PDResources resources, ASAtom name) {
        if (name.equals(ASAtom.DEVICECMYK) &&
                resources.hasColorSpace(ASAtom.DEFAULT_CMYK)) {
            return ASAtom.DEFAULT_CMYK;
        } else if (name.equals(ASAtom.DEVICERGB) &&
                resources.hasColorSpace(ASAtom.DEFAULT_RGB)) {
            return ASAtom.DEFAULT_RGB;
        } else if (name.equals(ASAtom.DEVICEGRAY) &&
                resources.hasColorSpace(ASAtom.DEFAULT_GRAY)) {
            return ASAtom.DEFAULT_GRAY;
        } else {
            return null;
        }
    }

    public static PDColorSpace getColorSpace(COSObject base) {
        return getColorSpace(base, null);
    }

    public static PDColorSpace getColorSpace(COSObject base, PDResources resources) {
        if (base == null) {
            return null;
        }
        COSObjType type = base.getType();
        if (type == COSObjType.COS_NAME) {
            PDColorSpace cs = getColorSpaceFromName(base);
            if (cs == null) {
                if (resources != null) {
                    cs = resources.getColorSpace(base.getName());
                    return cs;
                }
            }
            return cs;
        } else if (type == COSObjType.COS_ARRAY) {
            return getColorSpaceFromArray(base);
        } else if (type != null && type.isDictionaryBased()) {
            return getPattern(base);
        } else {
            LOGGER.log(Level.FINE, "COSObject has to be a name or array, but it is not");
            return null;
        }
    }

    private static PDColorSpace getColorSpaceFromName(COSObject base) {
        ASAtom name = base.getName();
        if (ASAtom.DEVICEGRAY.equals(name)) {
            return PDDeviceGray.INSTANCE;
        } else if (ASAtom.DEVICERGB.equals(name)) {
            return PDDeviceRGB.INSTANCE;
        } else if (ASAtom.DEVICECMYK.equals(name)) {
            return PDDeviceCMYK.INSTANCE;
        } else if (ASAtom.PATTERN.equals(name)) {
            return PDPattern.INSTANCE;
        } else {
            LOGGER.log(Level.FINE, "Unknown ColorSpace name");
            return null;
        }
    }

    private static PDColorSpace getColorSpaceFromArray(COSObject base) {
        if (base.size().intValue() < 2) {
            LOGGER.log(Level.FINE, "ColorSpace array can not contain less than two elements");
            return null;
        }
        ASAtom name = base.at(0).getName();
        if (ASAtom.CALGRAY.equals(name)) {
            return new PDCalGray(base);
        } else if (ASAtom.CALRGB.equals(name)) {
            return new PDCalRGB(base);
        } else if (ASAtom.LAB.equals(name)) {
            return new PDLab(base);
        } else if (ASAtom.ICCBASED.equals(name)) {
            return new PDICCBased(base);
        } else if (ASAtom.SEPARATION.equals(name)) {
            return new PDSeparation(base);
        } else if (ASAtom.DEVICEN.equals(name)) {
            return new PDDeviceN(base);
        } else if (ASAtom.INDEXED.equals(name)) {
            return new PDIndexed(base);
        } else if (ASAtom.PATTERN == name) {
            return PDPattern.INSTANCE;
        } else {
            LOGGER.log(Level.FINE, "Unknown ColorSpace name");
            return null;
        }
    }

    private static PDPattern getPattern(COSObject base) {
        Long patternType = base.getIntegerKey(ASAtom.PATTERN_TYPE);
        if (patternType != null) {
            int simplePatternType = patternType.intValue();
            switch (simplePatternType) {
                case PDPattern.TYPE_TILING_PATTERN:
                    return new PDTilingPattern(base);
                case PDPattern.TYPE_SHADING_PATTERN:
                    return new PDShadingPattern(base);
                default:
                    LOGGER.log(Level.FINE, "PatternType value is not correct");
                    return null;
            }
        }
		LOGGER.log(Level.FINE, "COSObject doesn't contain PatternType key");
		return null;
    }

}
