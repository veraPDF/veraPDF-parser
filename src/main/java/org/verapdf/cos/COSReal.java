/**
 * This file is part of veraPDF Parser, a module of the veraPDF project.
 * Copyright (c) 2015-2025, veraPDF Consortium <info@verapdf.org>
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

import org.verapdf.cos.visitor.ICOSVisitor;
import org.verapdf.cos.visitor.IVisitor;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

/**
 * @author Timur Kamalov
 */
public class COSReal extends COSNumber {

    private static final DecimalFormat FORMATTER;

    static {
        FORMATTER = new DecimalFormat("#0.000000");
        DecimalFormatSymbols decFormSymbols = FORMATTER.getDecimalFormatSymbols();
        decFormSymbols.setDecimalSeparator('.');
        FORMATTER.setDecimalFormatSymbols(decFormSymbols);
    }

    private double value;

    protected COSReal() {
    }

    protected COSReal(final double value) {
        this.value = value;
    }

    @Override
    public COSObjType getType() {
        return COSObjType.COS_REAL;
    }

    public static COSObject construct(final double initValue) {
        return new COSObject(new COSReal(initValue));
    }

    @Override
    public void accept(final IVisitor visitor) {
        visitor.visitFromReal(this);
    }

    @Override
    public Object accept(final ICOSVisitor visitor) {
        return visitor.visitFromReal(this);
    }

    @Override
    public Long getInteger() {
        return (long) get();
    }

    public void setInteger(final int value) {
        set(value);
    }

    @Override
    public Double getReal() {
        return get();
    }

    @Override
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
        String stringValue = FORMATTER.format(this.value);
        // remove fraction digit "0" only
        if (stringValue.indexOf('.') > -1 && !stringValue.endsWith(".0"))
        {
            while (stringValue.endsWith("0") && !stringValue.endsWith(".0"))
            {
                stringValue = stringValue.substring(0, stringValue.length() - 1);
            }
        }
        return stringValue;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof COSReal)) return false;

        COSReal cosReal = (COSReal) o;

        return Double.compare(cosReal.value, value) == 0;

    }
}
