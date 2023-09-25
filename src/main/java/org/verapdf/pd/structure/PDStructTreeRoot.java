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
import org.verapdf.cos.COSObjType;
import org.verapdf.cos.COSObject;
import org.verapdf.tools.StaticResources;
import org.verapdf.tools.TaggedPDFHelper;

import java.util.*;

/**
 * @author Maksim Bezrukov
 */
public class PDStructTreeRoot extends PDStructTreeNode {

	public PDStructTreeRoot(COSObject obj) {
		super(obj);
		StaticResources.setRoleMapHelper(getRoleMap());
	}

	@Override
	public List<PDStructElem> getStructChildren() {
		return TaggedPDFHelper.getStructTreeRootStructChildren(getObject(), getRoleMap());
	}

	@Override
	public List<Object> getChildren() {
		return TaggedPDFHelper.getStructTreeRootChildren(getObject(), getRoleMap());
	}

	public Map<ASAtom, ASAtom> getRoleMap() {
		COSObject roleMap = getKey(ASAtom.ROLE_MAP);
		if (roleMap != null && roleMap.getType() == COSObjType.COS_DICT && roleMap.size() > 0) {
			Map<ASAtom, ASAtom> res = new HashMap<>();
			Set<ASAtom> keys = roleMap.getKeySet();
			for (ASAtom key : keys) {
				ASAtom value = roleMap.getNameKey(key);
				if (value != null) {
					res.put(key, value);
				}
			}
			return Collections.unmodifiableMap(res);
		}
		return Collections.emptyMap();
	}

	public PDNumberTreeNode getParentTree() {
		COSObject parentTree = getKey(ASAtom.PARENT_TREE);
		if (parentTree != null && parentTree.getType().isDictionaryBased()) {
			return new PDNumberTreeNode(parentTree);
		}
		return null;
	}
}
