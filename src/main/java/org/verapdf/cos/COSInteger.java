package org.verapdf.cos;

import org.verapdf.cos.visitor.ICOSVisitor;
import org.verapdf.cos.visitor.IVisitor;

/**
 * @author Timur Kamalov
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

    public void accept(final IVisitor visitor) {
        visitor.visitFromInteger(this);
    }

    public Object accept(final ICOSVisitor visitor) {
        return visitor.visitFromInteger(this);
    }

    public Long getInteger() {
        return get();
    }

    public boolean setInteger(final long value) {
        set(value);
        return true;
    }

    public Double getReal() {
        return (double) get();
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
