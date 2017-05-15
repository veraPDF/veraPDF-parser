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