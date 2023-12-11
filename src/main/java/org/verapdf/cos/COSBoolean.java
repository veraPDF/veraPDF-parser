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

import org.verapdf.cos.visitor.ICOSVisitor;
import org.verapdf.cos.visitor.IVisitor;

/**
 * @author Timur Kamalov
 */
public class COSBoolean extends COSDirect {

    public static final COSBoolean TRUE = new COSBoolean(true);
    public static final COSBoolean FALSE = new COSBoolean(false);

    private boolean value;

    protected COSBoolean() {
    }

    protected COSBoolean(final boolean initValue) {
        super();
        this.value = initValue;
    }

    @Override
    public COSObjType getType() {
        return COSObjType.COS_BOOLEAN;
    }

    public static COSObject construct(final boolean initValue) {
        return new COSObject(new COSBoolean(initValue));
    }

    @Override
    public void accept(final IVisitor visitor) {
        visitor.visitFromBoolean(this);
    }

    @Override
    public Object accept(final ICOSVisitor visitor) {
        return visitor.visitFromBoolean(this);
    }

    @Override
    public Boolean getBoolean() {
        return get();
    }

    @Override
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof COSBoolean)) return false;

        COSBoolean that = (COSBoolean) o;

        return value == that.value;

    }
}
