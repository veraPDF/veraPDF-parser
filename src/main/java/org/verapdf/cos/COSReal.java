package org.verapdf.cos;

/**
 * Created by Timur on 12/18/2015.
 */
public class COSReal extends COSNumber {

    private double value;

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

    public double get() {
        return this.value;
    }

}
