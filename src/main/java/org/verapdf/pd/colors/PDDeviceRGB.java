package org.verapdf.pd.colors;

import org.verapdf.as.ASAtom;

/**
 * @author Maksim Bezrukov
 */
public class PDDeviceRGB extends PDColorSpace {

    public static final PDDeviceRGB INSTANCE = new PDDeviceRGB();

    private PDDeviceRGB() {
    }

    @Override
    public int getNumberOfComponents() {
        return 3;
    }

    @Override
    public ASAtom getName() {
        return ASAtom.DEVICERGB;
    }
}
