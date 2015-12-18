package org.verapdf.cos;

/**
 * Created by Timur on 12/17/2015.
 */
public class COSInteger extends COSNumber {

    private long value;

    protected COSInteger(final long value) {
        this.value = value;
    }

    public COSObjType getType() {
        return COSObjType.COSIntegerT;
    }

    public static COSObject construct(final long initValue) {
        return new COSObject(new COSInteger(initValue));

    }

}
