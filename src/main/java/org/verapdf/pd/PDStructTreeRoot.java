package org.verapdf.pd;

import org.verapdf.as.ASAtom;
import org.verapdf.cos.COSObjType;
import org.verapdf.cos.COSObject;
import org.verapdf.tools.TaggedPDFHelper;

import java.util.*;

/**
 * @author Maksim Bezrukov
 */
public class PDStructTreeRoot extends PDStructTreeNode {

	public PDStructTreeRoot(COSObject obj) {
		super(obj);
	}

	@Override
	public List<PDStructElem> getChildren() {
		return TaggedPDFHelper.getStructTreeRootChildren(getObject());
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
}
