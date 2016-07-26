package org.verapdf.pd.colors;

import org.verapdf.as.ASAtom;

/**
 * @author Maksim Bezrukov
 */
public class PDDeviceGray extends PDColorSpace {

    public static final PDDeviceGray INSTANCE = new PDDeviceGray();

    private PDDeviceGray() {
    }

    @Override
    public int getNumberOfComponents() {
        return 1;
    }

    @Override
    public ASAtom getType() {
        return ASAtom.DEVICEGRAY;
    }
}
