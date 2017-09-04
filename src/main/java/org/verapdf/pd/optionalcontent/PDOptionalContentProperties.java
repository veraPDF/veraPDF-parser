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
package org.verapdf.pd.optionalcontent;

import org.verapdf.as.ASAtom;
import org.verapdf.cos.COSArray;
import org.verapdf.cos.COSDictionary;
import org.verapdf.cos.COSObjType;
import org.verapdf.cos.COSObject;
import org.verapdf.pd.PDObject;

/**
 * @author Timur Kamalov
 */
public class PDOptionalContentProperties extends PDObject {

	public PDOptionalContentProperties(COSObject obj) {
		super(obj);
	}

	public String[] getGroupNames() {
		COSObject ocgs = getObject().getKey(ASAtom.OCGS);
		if (!ocgs.empty() && ocgs.getType() == COSObjType.COS_ARRAY) {
			COSArray ocgsArray = (COSArray) ocgs.getDirectBase();
			int size = ocgsArray.size();
			String[] groups = new String[size];

			for(int i = 0; i < size; ++i) {
				COSObject obj = ocgs.at(i);
				if (!obj.empty() && obj.getType() == COSObjType.COS_DICT) {
					COSDictionary ocgDict = (COSDictionary) obj.getDirectBase();
					String ocgName = ocgDict.getStringKey(ASAtom.NAME);
					groups[i] = ocgName == null ? "" : ocgName;
				}
			}

			return groups;
		} else {
			return null;
		}
	}

}
