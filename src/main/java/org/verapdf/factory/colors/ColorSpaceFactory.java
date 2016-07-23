package org.verapdf.factory.colors;

import org.apache.log4j.Logger;
import org.verapdf.as.ASAtom;
import org.verapdf.cos.COSObjType;
import org.verapdf.cos.COSObject;
import org.verapdf.pd.colors.*;

/**
 * @author Maksim Bezrukov
 */
public class ColorSpaceFactory {

    private ColorSpaceFactory() {
    }

    private static final Logger LOGGER = Logger.getLogger(ColorSpaceFactory.class);

    public static PDColorSpace getColorSpace(COSObject base) {
        if (base == null) {
            return null;
        }
        COSObjType type = base.getType();
        if (type == COSObjType.COS_NAME) {
            return getColorSpaceFromName(base);
        } else if (type == COSObjType.COS_ARRAY) {
            return getColorSpaceFromArray(base);
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
        } else {
            LOGGER.debug("Unknown ColorSpace name");
            return null;
        }
    }

}
