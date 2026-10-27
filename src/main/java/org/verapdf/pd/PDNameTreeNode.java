/*
 * This file is part of veraPDF Parser, a module of the veraPDF project.
 * Copyright (c) 2015-2026, veraPDF Consortium <info@verapdf.org>
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
import org.verapdf.cos.*;
import org.verapdf.exceptions.LoopedException;

import java.util.*;

/**
 * @author Maksim Bezrukov
 */
public class PDNameTreeNode extends PDObject implements Iterable<COSObject> {

	private final Set<COSKey> parents;

	private COSString[] limitsArray = null;
	private List<PDNameTreeNode> kids = null;
	private Map<COSString, COSObject> names = null;

	private PDNameTreeNode(COSObject obj, Set<COSKey> parents) {
		super(obj);
		COSKey objectKey = obj.getObjectKey();
		this.parents = new HashSet<>(parents);
		if (objectKey != null) {
			if (parents.contains(objectKey)) {
				throw new LoopedException("Loop in name tree");
			}
			this.parents.add(objectKey);
		}
	}

	public static PDNameTreeNode create(COSObject object) {
		if (object == null || !object.getType().isDictionaryBased()) {
			throw new IllegalArgumentException("Argument object shall be dictionary or stream type");
		}

		return new PDNameTreeNode(object, new HashSet<>());
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

	public Map<COSString, COSObject> getNames() {
		if (this.names == null) {
			this.names = parseNames();
		}
		return Collections.unmodifiableMap(this.names);
	}

	private Map<COSString, COSObject> parseNames() {
		COSObject names = getKey(ASAtom.NAMES);
		if (names != null && names.getType() == COSObjType.COS_ARRAY) {
			// COSString.equals also compares hex flags, so name keys are matched by their bytes
			Map<COSString, COSObject> res = new TreeMap<>(COSString::compare);
			for (int i = 0; i < names.size(); i += 2) {
				COSObject keyObj = names.at(i);
				if (keyObj.getType() == COSObjType.COS_STRING) {
					COSString key = (COSString) keyObj.getDirectBase();
					if (key != null) {
						COSObject value = names.at(i + 1);
						res.put(key, value);
					}
				}
			}
			return res;
		}
		return Collections.emptyMap();
	}

	public COSString[] getLimitsArray() {
		if (limitsArray == null) {
			limitsArray = parseLimitsArray();
		}
		return limitsArray;
	}

	public COSString[] parseLimitsArray() {
		COSObject limits = this.getKey(ASAtom.LIMITS);
		if (limits != null && !limits.empty() && limits.getType() == COSObjType.COS_ARRAY
				&& limits.size() >= 2 && limits.at(0).getType() == COSObjType.COS_STRING
				&& limits.at(1).getType() == COSObjType.COS_STRING) {
			COSString[] res = new COSString[2];
			res[0] = (COSString) limits.at(0).getDirectBase();
			res[1] = (COSString) limits.at(1).getDirectBase();
			return res;
		}
		return new COSString[0];
	}

	public COSObject getObject(COSString key) {
		COSString[] limits = this.getLimitsArray();
		if (limits.length == 2 && (COSString.compare(key, limits[0]) < 0 || COSString.compare(key, limits[1]) > 0)) {
			// string not in the limits
			return null;
		}

		if (this.knownKey(ASAtom.NAMES)) {
			// just get object from names or check if it is not in names
			Map<COSString, COSObject> names = getNames();
			return names == null ? null : names.get(key);
		}

		if (this.knownKey(ASAtom.KIDS)) {
			// find kid with mapping for given key
			List<PDNameTreeNode> kids = getKids();
			if (kids != null) {
				for (PDNameTreeNode kid : kids) {
					COSObject res = kid.getObject(key);
					if (res != null) {
						return res;
					}
				}
			}
		}
		return null;
	}

	private List<COSObject> getObjects() {
		List<COSObject> result = new LinkedList<>(getNames().values());
		for (PDNameTreeNode kid : getKids()) {
			result.addAll(kid.getObjects());
		}
		return result;
	}

	@Override
	public Iterator<COSObject> iterator() {
		return getObjects().iterator();
	}

	public Long size() {
		long i = 0;
		Iterator<COSObject> iterator = iterator();
		for (; iterator.hasNext(); i++) {
			iterator.next();
		}
		return i;
	}
	
	public boolean containsKey(COSString key) {
		return getObject(key) != null;
	}
	
	public boolean containsValue(COSObject value) {
		for (COSObject object : this) {
			if (object != null && object.equals(value)) {
				return true;
			}
		}
		return false;
	}
}
