package org.verapdf.pd.colors;

import org.verapdf.as.ASAtom;

/**
 * @author Maksim Bezrukov
 */
public class PDDeviceCMYK extends PDColorSpace {

    public static final PDDeviceCMYK INSTANCE = new PDDeviceCMYK(false);
    public static final PDDeviceCMYK INHERITED_INSTANCE = new PDDeviceCMYK(true);

    private PDDeviceCMYK(boolean isInherited) {
        setInherited(isInherited);
    }

    @Override
    public int getNumberOfComponents() {
        return 4;
    }

    @Override
    public ASAtom getType() {
        return ASAtom.DEVICECMYK;
    }
}
