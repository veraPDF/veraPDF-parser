package org.verapdf.pd.colors;

import org.verapdf.as.ASAtom;

/**
 * @author Maksim Bezrukov
 */
public class PDDeviceCMYK extends PDColorSpace {

    public static final PDDeviceCMYK INSTANCE = new PDDeviceCMYK();

    private PDDeviceCMYK() {
    }

    @Override
    public int getNumberOfComponents() {
        return 4;
    }

    @Override
    public ASAtom getName() {
        return ASAtom.DEVICECMYK;
    }
}
