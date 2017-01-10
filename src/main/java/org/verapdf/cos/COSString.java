/**
 * This file is part of veraPDF Parser, a module of the veraPDF project.
 * Copyright (c) 2015, veraPDF Consortium <info@verapdf.org>
 * All rights reserved.
 *
 * veraPDF Parser is free software: you can redistribute it and/or modify
 * it under the terms of either:
 *
 * The GNU General public license GPLv3+.
 * You should have received a copy of the GNU General Public License
 * along with veraPDF Parser as the LICENSE.GPL file in the root of the source
 * tree.  If not, see http://www.gnu.org/licenses/ or
 * https://www.gnu.org/licenses/gpl-3.0.en.html.
 *
 * The Mozilla Public License MPLv2+.
 * You should have received a copy of the Mozilla Public License along with
 * veraPDF Parser as the LICENSE.MPL file in the root of the source tree.
 * If a copy of the MPL was not distributed with this file, you can obtain one at
 * http://mozilla.org/MPL/2.0/.
 */
package org.verapdf.cos;

import org.verapdf.cos.filters.COSFilterASCIIHexEncode;
import org.verapdf.cos.visitor.ICOSVisitor;
import org.verapdf.cos.visitor.IVisitor;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Timur Kamalov
 */
public class COSString extends COSDirect {

    private static final Logger LOGGER = Logger.getLogger(COSString.class.getCanonicalName());

    private byte[] value;
    private boolean isHex;

    //fields specific for pdf/a validation of strings
    private boolean containsOnlyHex = true;
    private long hexCount = 0;

    public COSString() {
        super();
        this.value = new byte[0];
        this.isHex = false;
    }

    public COSString(byte[] value) {
        this(value, false);
    }

    public COSString(byte[] value, boolean isHex) {
        super();
        this.value = value;
        this.isHex = isHex;
    }

    public COSString(byte[] value, boolean isHex, long hexCount, boolean containsOnlyHex) {
        this(value, isHex);
        this.hexCount = hexCount;
        this.containsOnlyHex = containsOnlyHex;
    }

    public static COSObject construct(final byte[] initValue) {
        return construct(initValue, false);
    }

    public static COSObject construct(final byte[] initValue, final boolean isHex) {
        return new COSObject(new COSString(initValue, isHex));
    }

    public static COSObject construct(final byte[] initValue, final boolean isHex, final long hexCount, final boolean containsOnlyHex) {
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
        return (long) this.value.length;
    }

    public Double getReal() {
        return (double) this.value.length;
    }

    public String getString() {
        if (value.length > 2) {
            if ((value[0] & 0xff) == 0xFE && (value[1] & 0xff) == 0xFF) {
                return new String(value, 2, value.length - 2, Charset.forName("UTF-16BE"));
            }
        }
        return new String(value);
    }

    public boolean setString(final String value) {
        this.value = new byte[value.length()];
        boolean utf16 = false;
        for (int i = 0; i < value.length(); ++i) {
            char c = value.charAt(i);
            if (c <= 255) {
                this.value[i] = (byte) (c & 0xFF);
            } else {
                utf16 = true;
                break;
            }
        }
        if (utf16) {
            try {
                byte[] utfValue = value.getBytes("UTF-16BE");
                this.value = new byte[utfValue.length + 2];
                this.value[0] = (byte) 0xFE;
                this.value[1] = (byte) 0xFF;
                for(int i = 0; i < utfValue.length; ++i) {
                    this.value[i + 2] = utfValue[i];
                }
            } catch (UnsupportedEncodingException e) {
                LOGGER.log(Level.FINE, "Can't find encoding UTF-16BE", e);
                return false;
            }
        }
        return true;
    }

    public void setHex(boolean hex) {
        isHex = hex;
    }

    public boolean setString(final byte[] value, final boolean isHex) {
        this.value = value;
        this.isHex = isHex;
        return true;
    }

    public byte[] get() {
        return this.value;
    }

    public void set(final byte[] value) {
        this.value = value;
    }

    public boolean isLiteral() {
        return !isHex;
    }

    public boolean isHexadecimal() {
        return isHex;
    }

    public String getHexString() {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < this.value.length; i++) {
            final int c = this.value[i] & 0xFF;
            result.append(COSFilterASCIIHexEncode.asciiHexBig[c]);
            result.append(COSFilterASCIIHexEncode.asciiHexLittle[c]);
        }
        return result.toString();
    }

    @Override
    public String toString() {
        return this.isHex ? toHexString() : getString();
    }

    protected String toHexString() {
        StringBuilder result = new StringBuilder();

        result.append('<');
        for (int i = 0; i < this.value.length; i++) {
            final int c = this.value[i] & 0xFF;
            result.append(COSFilterASCIIHexEncode.asciiHexBig[c]);
            result.append(COSFilterASCIIHexEncode.asciiHexLittle[c]);
        }
        result.append('>');

        return result.toString();
    }

    protected String toLitString() {
        StringBuilder result = new StringBuilder();
        result.append('(');
        for (int i = 0; i < this.value.length; i++) {
            final byte ch = this.value[i];
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
