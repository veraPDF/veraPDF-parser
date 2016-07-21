package org.verapdf.pd.colors;

/**
 * @author Maksim Bezrukov
 */
public class PDDeviceGray extends PDColorSpace {

    public static final PDDeviceGray INSTANCE = new PDDeviceGray();

    @Override
    public int getNumberOfComponents() {
        return 1;
    }
}
