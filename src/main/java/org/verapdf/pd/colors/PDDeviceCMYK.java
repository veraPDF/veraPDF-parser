package org.verapdf.pd.colors;

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
}
