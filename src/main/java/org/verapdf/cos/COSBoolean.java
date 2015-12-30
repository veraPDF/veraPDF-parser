package org.verapdf.cos;

/**
 * Created by Timur on 12/18/2015.
 */
public class COSBoolean extends COSDirect {

    private boolean value;

    protected COSBoolean() {
    }

    protected COSBoolean(final boolean initValue) {
        super();
        this.value = initValue;
    }

    public COSObjType getType() {
        return COSObjType.COSBooleanT;
    }

    public static COSObject construct(final boolean initValue) {
        return new COSObject(new COSBoolean(initValue));
    }

    public boolean getBoolean() {
        return this.value;
    }

    public boolean setBoolean(final boolean value) {
        set(value);
        return true;
    }

    public boolean get() {
        return this.value;
    }

    public void set(final boolean value) {
        this.value = value;
    }

}
