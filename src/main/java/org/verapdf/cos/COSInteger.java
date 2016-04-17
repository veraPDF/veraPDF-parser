package org.verapdf.cos;

import org.verapdf.cos.visitor.ICOSVisitor;
import org.verapdf.cos.visitor.IVisitor;

/**
 * Created by Timur on 12/17/2015.
 */
public class COSInteger extends COSNumber {

    private long value;

    protected COSInteger() {
    }

    protected COSInteger(final long value) {
        this.value = value;
    }

    public COSObjType getType() {
        return COSObjType.COSIntegerT;
    }

    public static COSObject construct(final long initValue) {
        return new COSObject(new COSInteger(initValue));
    }

    public void accept(final IVisitor visitor) {
        visitor.visitFromInteger(this);
    }

    public Object accept(final ICOSVisitor visitor) {
        return visitor.visitFromInteger(this);
    }

    public long getInteger() {
        return get();
    }

    public boolean setInteger(final long value) {
        set(value);
        return true;
    }

    public double getReal() {
        return get();
    }

    public boolean setReal(final double value) {
        set((long) value);
        return true;
    }

    public long get() {
        return this.value;
    }

    public void set(final long value) {
        this.value = value;
    }

    public String toString() {
        return String.valueOf(this.value);
    }

}
