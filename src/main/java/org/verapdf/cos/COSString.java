package org.verapdf.cos;

import org.verapdf.cos.filters.COSFilterASCIIHexEncode;
import org.verapdf.cos.visitor.ICOSVisitor;
import org.verapdf.cos.visitor.IVisitor;

/**
 * @author Timur Kamalov
 */
public class COSString extends COSDirect {

    private String value;
    private boolean isHex;

    //fields specific for pdf/a validation of strings
    private boolean containsOnlyHex = true;
    private long hexCount = 0;

    public COSString() {
        super();
        this.value = new String();
        this.isHex = false;
    }

    public COSString(String value) {
        this(value, false);
    }

    public COSString(String value, boolean isHex) {
        super();
        this.value = value;
        this.isHex = isHex;
    }

    public COSString(String value, boolean isHex, long hexCount, boolean containsOnlyHex) {
        this(value, isHex);
        this.hexCount = hexCount;
        this.containsOnlyHex = containsOnlyHex;
    }

    public static COSObject construct(final String initValue) {
        return construct(initValue, false);
    }

    public static COSObject construct(final String initValue, final boolean isHex) {
        return new COSObject(new COSString(initValue, isHex));
    }

    public static COSObject construct(final String initValue, final boolean isHex, final long hexCount, final boolean containsOnlyHex) {
        return new COSObject(new COSString(initValue, isHex, hexCount, containsOnlyHex));
    }

    public void accept(final IVisitor visitor) {
        visitor.visitFromString(this);
    }

    public Object accept(final ICOSVisitor visitor) {
        return visitor.visitFromString(this);
    }

    public COSObjType getType() {
        return COSObjType.COS_STRING;
    }

    //! Returns the size of the string
    public Long getInteger() {
        return (long) this.value.length();
    }

    public Double getReal() {
        return (double) this.value.length();
    }

    public String getString() {
        return get();
    }

    public boolean setString(final String value) {
        setString(value, false);
        return true;
    }

    public void setHex(boolean hex) {
        isHex = hex;
    }

    public boolean setString(final String value, final boolean isHex) {
        this.value = value;
        this.isHex = isHex;
        return true;
    }

    public String get() {
        return this.value;
    }

    public void set(final String value) {
        this.value = value;
    }

    public boolean isLiteral() {
        return !isHex;
    }

    public boolean isHexadecimal() {
        return isHex;
    }

    @Override
    public String toString() {
        return this.isHex ? toHexString() : toLitString();
    }

    protected String toHexString() {
        StringBuilder result = new StringBuilder();

        result.append('<');
        for (int i = 0; i < this.value.length(); i++) {
            final char c = this.value.charAt(i);
            result.append(COSFilterASCIIHexEncode.asciiHexBig[c]);
            result.append(COSFilterASCIIHexEncode.asciiHexLittle[c]);
        }
        result.append('>');

        return result.toString();
    }

    protected String toLitString() {
        StringBuilder result = new StringBuilder();
        result.append('(');
        for (int i = 0; i < this.value.length(); i++) {
            final char ch = this.value.charAt(i);
            switch (ch) {
                case '(':
                    result.append("\\(");
                    break;
                case ')':
                    result.append("\\)");
                    break;
                case '\n':
                    result.append('\n');
                    break;
                case '\r':
                    result.append('\r');
                    break;
                case '\t':
                    result.append('\t');
                    break;
                case '\b':
                    result.append('\b');
                    break;
                case '\f':
                    result.append('\f');
                    break;
                case '\\':
                    result.append('\\');
                    break;
                default:
                    result.append(ch);
                    break;
            }
        }
        result.append(')');

        return result.toString();
    }

    public String getLitString() {
        return toLitString();
    }

    public boolean isContainsOnlyHex() {
        return containsOnlyHex;
    }

    public void setContainsOnlyHex(boolean containsOnlyHex) {
        this.containsOnlyHex = containsOnlyHex;
    }

    public long getHexCount() {
        return hexCount;
    }

    public void setHexCount(long hexCount) {
        this.hexCount = hexCount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof COSString)) return false;

        COSString cosString = (COSString) o;

        if (isHex != cosString.isHex) return false;
        if (containsOnlyHex != cosString.containsOnlyHex) return false;
        if (hexCount != cosString.hexCount) return false;
        return value != null ? value.equals(cosString.value) : cosString.value == null;

    }

    @Override
    public int hashCode() {
        int result = value != null ? value.hashCode() : 0;
        result = 31 * result + (isHex ? 1 : 0);
        result = 31 * result + (containsOnlyHex ? 1 : 0);
        result = 31 * result + (int) (hexCount ^ (hexCount >>> 32));
        return result;
    }
}
