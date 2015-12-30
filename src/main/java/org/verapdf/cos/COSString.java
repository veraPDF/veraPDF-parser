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

    public COSString(String value) {
        this(value, false);
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

    //! Returns the size of the string
    public long getInteger() {
        return this.value.length();
    }

    public double getReal() {
        return this.value.length();
    }

    public String getString() {
        return get();
    }

    public boolean setString(final String value) {
        setString(value, false);
        return true;
    }

    public boolean setString(final String value, final boolean isHex) {
        this.value = value;
        this.isHex = isHex;
        return true;
    }

    public String get() {
        return this.value;
    }

    public void set(final String value) {
        this.value = value;
    }

    public boolean isLiteral() {
        return !isHex;
    }

    public boolean isHexadecimal() {
        return isHex;
    }

}
