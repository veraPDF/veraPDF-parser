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
package org.verapdf.pd.structure;

import org.verapdf.as.ASAtom;
import org.verapdf.cos.COSKey;
import org.verapdf.cos.COSObjType;
import org.verapdf.cos.COSObject;
import org.verapdf.pd.PDObject;
import org.verapdf.tools.StaticResources;

/**
 * Represents namespace in structure tree, as described in PDF-2.0 specification
 * 14.7.4.
 *
 * @author Sergey Shemyakov
 */
public class PDStructureNameSpace extends PDObject {

	private PDNameSpaceRoleMapping nsRoleMap;

	private PDStructureNameSpace(COSObject obj) {
		super(obj);
		COSObject roleMap = this.getKey(ASAtom.ROLE_MAP_NS);
		if (obj != null && obj.getType() == COSObjType.COS_DICT) {
			this.nsRoleMap = new PDNameSpaceRoleMapping(roleMap);
		} else {
			this.nsRoleMap = null;
		}
	}

	/**
	 * @return the string defining the namespace name.
	 */
	public String getNS() {
		COSObject obj = this.getKey(ASAtom.NS);
		if (obj != null && obj.getType() == COSObjType.COS_STRING) {
			return obj.getString();
		}
		return null;
	}

	public PDNameSpaceRoleMapping getNameSpaceMapping() {
		return this.nsRoleMap;
	}

	/**
	 * Returns PDStructureNameSpace object for given COSObject.
	 *
	 * @param obj COSObject of name space.
	 * @return PD object for name space.
	 */
	static PDStructureNameSpace createNameSpace(COSObject obj) {
		if (obj == null || obj.getType() != COSObjType.COS_DICT) {
			throw new IllegalArgumentException("COSObject argument should be dictionary type");
		}
		COSKey key = obj.getObjectKey();
		if (key == null) {
			throw new IllegalArgumentException("COSObject argument can not be direct");
		}
		PDStructureNameSpace res = StaticResources.getStructureNameSpace(key);
		if (res == null) {
			res = new PDStructureNameSpace(obj);
			StaticResources.cacheStructureNameSpace(res);
		}
		return res;
	}
}