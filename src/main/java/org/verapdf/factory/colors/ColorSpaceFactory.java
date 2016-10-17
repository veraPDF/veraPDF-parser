package org.verapdf.factory.colors;

import org.apache.log4j.Logger;
import org.verapdf.as.ASAtom;
import org.verapdf.cos.COSObjType;
import org.verapdf.cos.COSObject;
import org.verapdf.pd.PDResources;
import org.verapdf.pd.colors.*;
import org.verapdf.pd.patterns.PDPattern;
import org.verapdf.pd.patterns.PDShadingPattern;
import org.verapdf.pd.patterns.PDTilingPattern;

/**
 * @author Maksim Bezrukov
 */
public class ColorSpaceFactory {

    private static final Logger LOGGER = Logger.getLogger(ColorSpaceFactory.class);

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
            LOGGER.debug("COSObject has to be a name or array, but it is not");
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
            LOGGER.debug("Unknown ColorSpace name");
            return null;
        }
    }

    private static PDColorSpace getColorSpaceFromArray(COSObject base) {
        if (base.size() < 2) {
            LOGGER.debug("ColorSpace array can not contain less than two elements");
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
        } else {
            LOGGER.debug("Unknown ColorSpace name");
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
                    LOGGER.debug("PatternType value is not correct");
                    return null;
            }
        } else {
            LOGGER.debug("COSObject doesn't contain PatternType key");
            return null;
        }
    }

}
