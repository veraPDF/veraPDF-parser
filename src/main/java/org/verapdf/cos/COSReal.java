package org.verapdf.cos;

import org.verapdf.cos.visitor.ICOSVisitor;
import org.verapdf.cos.visitor.IVisitor;

import java.text.DecimalFormat;
import java.text.NumberFormat;

/**
 * Created by Timur on 12/18/2015.
 */
public class COSReal extends COSNumber {

    private static final NumberFormat formatter = new DecimalFormat("#0.000000");

    private double value;

    protected COSReal() {
    }

    protected COSReal(final double value) {
        this.value = value;
    }

    public COSObjType getType() {
        return COSObjType.COSRealT;
    }

    public static COSObject construct(final double initValue) {
        return new COSObject(new COSReal(initValue));
    }

    public void accept(final IVisitor visitor) {
        visitor.visitFromReal(this);
    }

    public Object accept(final ICOSVisitor visitor) {
        return visitor.visitFromReal(this);
    }

    public Long getInteger() {
        return (long) get();
    }

    public boolean setInteger(final int value) {
        set(value);
        return true;
    }

    public Double getReal() {
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
        String stringValue = formatter.format(this.value);
        // remove fraction digit "0" only
        if (stringValue.indexOf('.') > -1 && !stringValue.endsWith(".0"))
        {
            while (stringValue.endsWith("0") && !stringValue.endsWith(".0"))
            {
                stringValue = stringValue.substring(0,stringValue.length()-1);
            }
        }
        return stringValue;
    }

}
