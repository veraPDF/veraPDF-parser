package org.verapdf.cos;

/**
 * Created by Timur on 12/18/2015.
 */
public class COSReal extends COSNumber {

    private double value;

    protected COSReal() {
    }

    protected COSReal(final double value) {
        this.value = value;
    }

    public static COSObject construct(final double initValue) {
        return new COSObject(new COSReal(initValue));
    }

    public COSObjType getType() {
        return COSObjType.COSRealT;
    }

    public long getInteger() {
        return (long) get();
    }

    public boolean setInteger(final int value) {
        set(value);
        return true;
    }

    public double getReal() {
        return get();
    }

    public boolean setReal(final double value) {
        set(value);
        return true;
    }

    public double get() {
        return this.value;
    }

    public void set(final double value) {
        this.value = value;
    }

    public String toString() {
        return String.valueOf(this.value);
    }

}
