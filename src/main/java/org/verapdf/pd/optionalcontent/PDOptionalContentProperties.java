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
package org.verapdf.pd.optionalcontent;

import org.verapdf.as.ASAtom;
import org.verapdf.cos.*;
import org.verapdf.pd.PDObject;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Timur Kamalov
 */
public class PDOptionalContentProperties extends PDObject {

	public PDOptionalContentProperties(COSObject obj) {
		super(obj);
	}

	public List<String> getGroupNames() {
		COSObject ocgs = getObject().getKey(ASAtom.OCGS);
        List<String> groups = new ArrayList<>();
		if (!ocgs.empty() && ocgs.getType() == COSObjType.COS_ARRAY) {

            for (COSObject obj : (COSArray) ocgs.getDirectBase()) {
                if (!obj.empty() && obj.getType() == COSObjType.COS_DICT) {
                    String ocgName = obj.getStringKey(ASAtom.NAME);
                    groups.add(ocgName == null ? "" : ocgName);
                }
            }
        }

        return groups;
	}

    public boolean isContainsName(String name) {
        return getGroupNames().contains(name);
    }

    public boolean isVisibleLayer(String name) {
        if (name == null) {
            return true;
        }
        COSObject d = getObject().getKey(ASAtom.D);
        if (d != null && d.getType() == COSObjType.COS_DICT) {
            COSDictionary dict = (COSDictionary) d.getDirectBase();
            if (isDContainsOCGWithName(dict, ASAtom.ON, name)) {
                return true;
            }
            if (isDContainsOCGWithName(dict, ASAtom.OFF, name)) {
                return false;
            }

            ASAtom baseState = dict.getNameKey(ASAtom.BASE_STATE);
            if (baseState != null) {
                return baseState.equals(ASAtom.ON);
            }
        }
        return true;
    }

    private static boolean isDContainsOCGWithName(COSDictionary dict, ASAtom state, String name) {
        COSObject cosObject = dict.getKey(state);
        if (cosObject != null && cosObject.getType() == COSObjType.COS_ARRAY) {
            for (COSObject obj : (COSArray) cosObject.getDirectBase()) {
                if (!obj.empty() && obj.getType() == COSObjType.COS_DICT) {
                    String ocgName = obj.getStringKey(ASAtom.NAME);
                    if (name.equals(ocgName)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
