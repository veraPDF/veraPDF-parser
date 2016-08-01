package org.verapdf.font;

/**
 * Instance of this class can represent int or float.
 *
 * @author Sergey Shemyakov
 */
public class GeneralNumber {

    private long integer;
    private float real;
    boolean isInteger;

    public GeneralNumber(int integer) {
        this.integer = integer;
        this.isInteger = true;
    }

    public GeneralNumber(float real) {
        this.real = real;
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
