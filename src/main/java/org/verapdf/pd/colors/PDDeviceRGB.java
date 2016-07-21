package org.verapdf.pd.colors;

/**
 * @author Maksim Bezrukov
 */
public class PDDeviceRGB extends PDColorSpace {

    public static final PDDeviceRGB INSTANCE = new PDDeviceRGB();

    @Override
    public int getNumberOfComponents() {
        return 3;
    }
}
