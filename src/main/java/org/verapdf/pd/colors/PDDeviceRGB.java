package org.verapdf.pd.colors;

import org.verapdf.as.ASAtom;

/**
 * @author Maksim Bezrukov
 */
public class PDDeviceRGB extends PDColorSpace {

    public static final PDDeviceRGB INSTANCE = new PDDeviceRGB(false);
    public static final PDDeviceRGB INHERITED_INSTANCE = new PDDeviceRGB(true);

    private PDDeviceRGB(boolean isInherited) {
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
}
