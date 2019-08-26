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
package org.verapdf.pd.colors;

import org.verapdf.as.ASAtom;
import org.verapdf.as.io.ASInputStream;
import org.verapdf.as.io.ASMemoryInStream;
import org.verapdf.cos.COSObjType;
import org.verapdf.cos.COSObject;
import org.verapdf.cos.COSStream;
import org.verapdf.pd.PDResources;

import java.nio.charset.StandardCharsets;

/**
 * @author Maksim Bezrukov
 */
public class PDIndexed extends PDSpecialColorSpace {

    public PDIndexed(COSObject obj) {
        this(obj, null);
    }

    public PDIndexed(COSObject obj, PDResources resources) {
        super(obj, resources, false);
    }

    public PDColorSpace getBase() {
        return super.getBaseColorSpace();
    }

    @Override
    protected COSObject getBaseColorSpaceObject() {
        return getObject().at(1);
    }

    public Long getHival() {
        return getObject().at(2).getInteger();
    }

    public ASInputStream getLookup() {
        COSObject object = getObject().at(3);
        if (object != null) {
            COSObjType type = object.getType();
            if (type == COSObjType.COS_STRING) {
                return new ASMemoryInStream(object.getString().getBytes(StandardCharsets.ISO_8859_1));
            } else if (type == COSObjType.COS_STREAM) {
                return object.getData(COSStream.FilterFlags.DECODE);
            }
        }
        return null;
    }

    @Override
    public int getNumberOfComponents() {
        return 1;
    }

    @Override
    public ASAtom getType() {
        return ASAtom.INDEXED;
    }
}
