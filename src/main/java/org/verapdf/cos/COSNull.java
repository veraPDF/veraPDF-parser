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
public class COSNull extends COSDirect {

    public static final COSNull NULL = new COSNull();

    public COSObjType getType() {
        return COSObjType.COS_NULL;
    }

    public static COSObject construct() {
        return new COSObject(new COSNull());
    }

    public void accept(final IVisitor visitor) {
        visitor.visitFromNull(this);
    }

    public Object accept(final ICOSVisitor visitor) {
        return visitor.visitFromNull(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        return o instanceof COSNull;
    }
}
