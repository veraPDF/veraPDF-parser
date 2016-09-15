package org.verapdf.pd;

import org.verapdf.as.ASAtom;
import org.verapdf.cos.COSArray;
import org.verapdf.cos.COSObjType;
import org.verapdf.cos.COSObject;

import java.util.*;

/**
 * @author Maksim Bezrukov
 */
public class PDNameTreeNode extends PDObject {

	private List<PDNameTreeNode> kids = null;
	private Map<String, COSObject> names = null;

	private PDNameTreeNode(COSObject obj) {
		super(obj);
	}

	public static PDNameTreeNode create(COSObject object) {
		if (object == null || !object.getType().isDictionaryBased()) {
			throw new IllegalArgumentException("Argument object shall be dictionary or stream type");
		}

		return new PDNameTreeNode(object);
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
					res.add(PDNameTreeNode.create(obj));
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
