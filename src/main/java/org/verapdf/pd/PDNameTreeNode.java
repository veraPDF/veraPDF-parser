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
public class PDNameTreeNode extends PDObject implements Iterable<COSObject> {

	private final Set<COSKey> parents;

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
			for (int i = 0; i < names.size(); i += 2) {
				COSObject keyObj = names.at(i);
				String key = keyObj == null ? null : keyObj.getString();
				if (key != null) {
					COSObject value = names.at(i + 1);
					res.put(key, value);
				}
			}
			return res;
		}
		return Collections.emptyMap();
	}

	public String[] getLimitsArray() {
		COSObject limits = this.getKey(ASAtom.LIMITS);
		if (limits != null && !limits.empty() && limits.getType() == COSObjType.COS_ARRAY
				&& limits.size() >= 2 && limits.at(0).getType() == COSObjType.COS_STRING
				&& limits.at(1).getType() == COSObjType.COS_STRING) {
			String[] res = new String[2];
			res[0] = limits.at(0).getString();
			res[1] = limits.at(1).getString();
			return res;
		}
		return null;
	}

	public COSObject getObject(String key) {
		Set<COSKey> visitedKeys = new HashSet<>();
		COSKey objectKey = getObject().getObjectKey();
		if (objectKey != null) {
			visitedKeys.add(objectKey);
		}
		return getObject(key, visitedKeys);
	}

	private COSObject getObject(String key, Set<COSKey> visitedKeys) {
		String[] limits = this.getLimitsArray();
		if (limits != null && (key.compareTo(limits[0]) < 0 || key.compareTo(limits[1]) > 0)) {
			// string not in the limits
			return null;
		}

		if (this.knownKey(ASAtom.NAMES)) {
			// just get object from names or check if it is not in names
			Map<String, COSObject> names = getNames();
			return names == null ? null : names.get(key);
		}

		if (this.knownKey(ASAtom.KIDS)) {
			// find kid with mapping for given key
			List<PDNameTreeNode> kids = getKids();
			if (kids != null) {
				for (PDNameTreeNode kid : kids) {
					COSKey kidObjectKey = kid.getObject().getObjectKey();
					if (kidObjectKey != null) {
						if (visitedKeys.contains(kidObjectKey)) {
							throw new LoopedException("Loop inside name tree");
						} else {
							visitedKeys.add(kidObjectKey);
						}
					}
					COSObject res = kid.getObject(key, visitedKeys);
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
	
	public boolean containsKey(String key) {
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
