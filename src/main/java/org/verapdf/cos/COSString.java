package org.verapdf.cos;

/**
 * Created by Timur on 12/18/2015.
 */
public class COSString extends COSDirect {

    private String value;
    private boolean isHex;

    public COSString() {
        super();
        this.value = new String();
        this.isHex = false;
    }

    public COSString(String value, boolean isHex) {
        super();
        this.value = value;
        this.isHex = isHex;
    }

    public static COSObject construct(final String initValue) {
        return construct(initValue, false);
    }

    public static COSObject construct(final String initValue, final boolean isHex) {
        return new COSObject(new COSString(initValue, isHex));
    }

    public COSObjType getType() {
        return COSObjType.COSStringT;
    }

}
