package org.verapdf.pd.colors;

import org.verapdf.as.ASAtom;

/**
 * @author Maksim Bezrukov
 */
public class PDDeviceGray extends PDColorSpace {

    public static final PDDeviceGray INSTANCE = new PDDeviceGray(false);
    public static final PDDeviceGray INHERITED_INSTANCE = new PDDeviceGray(true);

    private PDDeviceGray(boolean isInherited) {
        setInherited(isInherited);
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
