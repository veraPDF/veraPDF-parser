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
package org.verapdf.pd;

import org.verapdf.as.ASAtom;
import org.verapdf.cos.COSArray;
import org.verapdf.cos.COSKey;
import org.verapdf.cos.COSObjType;
import org.verapdf.cos.COSObject;
import org.verapdf.exceptions.LoopedException;

import java.util.*;

/**
 * @author Maksim Bezrukov
 */
public class PDNameTreeNode extends PDObject {

	private Set<COSKey> parents = null;

	private List<PDNameTreeNode> kids = null;
	private Map<String, COSObject> names = null;

	private PDNameTreeNode(COSObject obj, Set<COSKey> parents) {
		super(obj);
		COSKey objectKey = obj.getObjectKey();
		this.parents = new HashSet<>(parents);
		if (objectKey != null) {
			if (parents.contains(objectKey)) {
				throw new LoopedException("Loop in name tree");
			} else {
				this.parents.add(objectKey);
			}
		}
	}

	public static PDNameTreeNode create(COSObject object) {
		if (object == null || !object.getType().isDictionaryBased()) {
			throw new IllegalArgumentException("Argument object shall be dictionary or stream type");
		}

		return new PDNameTreeNode(object, new HashSet<COSKey>());
	}

	public List<PDNameTreeNode> getKids() {
		if (this.kids == null) {
			this.kids = parseKids();
		}
		return Collections.unmodifiableList(this.kids);
	}

	private List<PDNameTreeNode> parseKids() {
		COSObject kids = getKey(ASAtom.KIDS);
		if (kids != null && kids.getType() == COSObjType.COS_ARRAY) {
			List<PDNameTreeNode> res = new ArrayList<>();
			for (COSObject obj : (COSArray) kids.getDirectBase()) {
				if (obj != null && obj.getType().isDictionaryBased()) {
					res.add(new PDNameTreeNode(obj, this.parents));
				}
			}
			return res;
		}
		return Collections.emptyList();
	}

	public Map<String, COSObject> getNames() {
		if (this.names == null) {
			this.names = parseNames();
		}
		return Collections.unmodifiableMap(this.names);
	}

	private Map<String, COSObject> parseNames() {
		COSObject names = getKey(ASAtom.NAMES);
		if (names != null && names.getType() == COSObjType.COS_ARRAY) {
			Map<String, COSObject> res = new LinkedHashMap<>();
			for (int i = 0; i < names.size(); i+=2) {
				COSObject keyObj = names.at(i);
				String key = keyObj == null ? null : keyObj.getString();
				if (key != null) {
					COSObject value = names.at(i+1);
					res.put(key, value);
				}
			}
			return res;
		}
		return Collections.emptyMap();
	}
}
