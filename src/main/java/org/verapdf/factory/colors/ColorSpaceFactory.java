package org.verapdf.factory.colors;

import org.apache.log4j.Logger;
import org.verapdf.as.ASAtom;
import org.verapdf.as.io.ASInputStream;
import org.verapdf.cos.COSObjType;
import org.verapdf.cos.COSObject;
import org.verapdf.cos.COSStream;
import org.verapdf.io.ASMemoryInStream;
import org.verapdf.pd.colors.*;
import org.verapdf.pd.patterns.PDPattern;
import org.verapdf.pd.patterns.PDShadingPattern;
import org.verapdf.pd.patterns.PDTilingPattern;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Maksim Bezrukov
 */
public class ColorSpaceFactory {

    private static final Logger LOGGER = Logger.getLogger(ColorSpaceFactory.class);

    private ColorSpaceFactory() {
    }

    public static PDColorSpace getColorSpace(COSObject base) {
        if (base == null) {
            return null;
        }
        COSObjType type = base.getType();
        if (type == COSObjType.COS_NAME) {
            return getColorSpaceFromName(base);
        } else if (type == COSObjType.COS_ARRAY) {
            return getColorSpaceFromArray(base);
        } else if (type == COSObjType.COS_DICT || type == COSObjType.COS_STREAM) {
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
            return new PDCalGray(base.at(1));
        } else if (ASAtom.CALRGB.equals(name)) {
            return new PDCalRGB(base.at(1));
        } else if (ASAtom.LAB.equals(name)) {
            return new PDLab(base.at(1));
        } else if (ASAtom.ICCBASED.equals(name)) {
            return new PDICCBased(base.at(1));
        } else if (ASAtom.SEPARATION.equals(name)) {
            return new PDSeparation(base.at(1), ColorSpaceFactory.getColorSpace(base.at(2)), base.at(3));
        } else if (ASAtom.DEVICEN.equals(name)) {
            return new PDDeviceN(getListOfNames(base.at(1)), ColorSpaceFactory.getColorSpace(base.at(2)),
                    base.at(3), base.at(4));
        } else if (ASAtom.INDEXED.equals(name)) {
            return new PDIndexed(ColorSpaceFactory.getColorSpace(base.at(1)), base.at(2).getInteger(), getLookup(base.at(3)));
        } else {
            LOGGER.debug("Unknown ColorSpace name");
            return null;
        }
    }

    private static ASInputStream getLookup(COSObject object) {
        COSObjType type = object.getType();
        if (type == COSObjType.COS_STRING) {
            return new ASMemoryInStream(object.getString().getBytes());
        } else if (type == COSObjType.COS_STREAM) {
            return object.getData(COSStream.FilterFlags.DECODE);
        } else {
            if (!object.empty()) {
                LOGGER.debug("Unknown lookup type");
            }
            return null;
        }
    }

    private static List<COSObject> getListOfNames(COSObject object) {
        if (object.getType() == COSObjType.COS_ARRAY) {
            List<COSObject> names = new ArrayList<>();
            for (int i = 0; i < object.size(); ++i) {
                names.add(object.at(i));
            }
        }
        return null;
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
