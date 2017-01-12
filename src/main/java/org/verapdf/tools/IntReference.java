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
package org.verapdf.tools;

/**
 * Represents int value that can be passed by reference.
 *
 * @author Sergey Shemyakov
 */
public class IntReference {

    private int[] num = new int[1];

    /**
     * Default constructor that sets integer to 0.
     */
    public IntReference() {
        this(0);
    }

    /**
     * Constructor that sets integer to given value.
     *
     * @param num is integer that will be stored.
     */
    public IntReference(int num) {
        this.num[0] = num;
    }

    /**
     * @return integer that is represented by this reference.
     */
    public int get() {
        return num[0];
    }

    /**
     * Increments internal integer.
     */
    public void increment() {
        this.num[0]++;
    }

    /**
     * Decrements internal integer.
     */
    public void decrement() {
        this.num[0]--;
    }

    /**
     * Sets internal integer to given value.
     *
     * @param num is value.
     */
    public void set(int num) {
        this.num[0] = num;
    }

    /**
     * Checks if internal integer equals to another int.
     *
     * @return true if internal integer equals to passed value.
     */
    public boolean equals(int num) {
        return this.num[0] == num;
    }
}
