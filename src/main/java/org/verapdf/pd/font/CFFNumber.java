package org.verapdf.pd.font;

/**
 * Instance of this class can represent int or float.
 *
 * @author Sergey Shemyakov
 */
public class CFFNumber {

    private long integer;
    private float real;
    private boolean isInteger;

    public CFFNumber(int integer) {
        this.integer = integer;
        this.real = integer;
        this.isInteger = true;
    }

    public CFFNumber(float real) {
        this.real = real;
        this.integer = (long) real;
        this.isInteger = false;
    }

    public boolean isInteger() {
        return isInteger;
    }

    public long getInteger() {
        return integer;
    }

    public float getReal() {
        return real;
    }
}
