package org.verapdf.cos;

import org.verapdf.cos.visitor.IVisitor;

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

    public void accept(final IVisitor visitor) {
        visitor.visitFromBoolean(this);
    }

    public boolean getBoolean() {
        return get();
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
