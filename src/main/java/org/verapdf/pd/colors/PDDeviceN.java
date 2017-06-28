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
import org.verapdf.cos.COSObjType;
import org.verapdf.cos.COSObject;
import org.verapdf.pd.PDResources;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Maksim Bezrukov
 */
public class PDDeviceN extends PDSpecialColorSpace {

    private final List<COSObject> names;

    public PDDeviceN(COSObject obj) {
        this(obj, null, false);
    }

    public PDDeviceN(COSObject obj, PDResources resources, boolean wasDefault) {
        super(obj, resources, wasDefault);
        this.names = parseNames(obj.at(1));
    }

    public List<COSObject> getNames() {
        return this.names;
    }

    public PDColorSpace getAlternateSpace() {
        return super.getBaseColorSpace();
    }

    @Override
    protected COSObject getBaseColorSpaceObject() {
        return getObject().at(2);
    }

    public COSObject getTintTransform() {
        return getObject().at(3);
    }

    public COSObject getAttributes() {
        return getObject().at(4);
    }

    @Override
    public int getNumberOfComponents() {
        return names.size();
    }

    @Override
    public ASAtom getType() {
        return ASAtom.DEVICEN;
    }

    private static List<COSObject> parseNames(COSObject obj) {
        if (obj != null && obj.getType() == COSObjType.COS_ARRAY) {
            List<COSObject> names = new ArrayList<>(obj.size());
            for (int i = 0; i < obj.size(); ++i) {
                names.add(obj.at(i));
            }
            return Collections.unmodifiableList(names);
        }
        return Collections.emptyList();
    }
}
