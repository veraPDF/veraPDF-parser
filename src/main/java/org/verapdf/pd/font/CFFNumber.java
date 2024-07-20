/**
 * This file is part of veraPDF Parser, a module of the veraPDF project.
 * Copyright (c) 2015-2024, veraPDF Consortium <info@verapdf.org>
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
package org.verapdf.pd.font;

/**
 * Instance of this class can represent int or float. It is used in CFF fonts.
 *
 * @author Sergey Shemyakov
 */
public class CFFNumber {

    private final long integer;
    private final float real;
    private final boolean isInteger;

    /**
     * Initializes this number with integer.
     * @param integer is integer to initialize CFFNumber.
     */
    public CFFNumber(int integer) {
        this.integer = integer;
        this.real = integer;
        this.isInteger = true;
    }

    /**
     * Initializes this number with float.
     * @param real is float number to initialize CFFNumber.
     */
    public CFFNumber(float real) {
        this.real = real;
        this.integer = (long) real;
        this.isInteger = false;
    }

    /**
     * @return true if CFFNumber is initialized with integer.
     */
    public boolean isInteger() {
        return isInteger;
    }

    /**
     * @return integer if this number is initialized with integer.
     */
    public long getInteger() {
        return integer;
    }

    /**
     * @return float if this number is initialized with float.
     */
    public float getReal() {
        return real;
    }
}
