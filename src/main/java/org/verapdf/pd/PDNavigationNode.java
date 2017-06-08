package org.verapdf.pd;

import org.verapdf.as.ASAtom;
import org.verapdf.cos.COSObjType;
import org.verapdf.cos.COSObject;
import org.verapdf.pd.actions.PDAction;

/**
 * @author Maksim Bezrukov
 */
public class PDNavigationNode extends PDObject {

	public PDNavigationNode(COSObject obj) {
		super(obj);
	}

	public PDAction getNA() {
		return getAction(ASAtom.NA);
	}

	public PDAction getPA() {
		return getAction(ASAtom.PA);
	}

	private PDAction getAction(ASAtom key) {
		COSObject object = getKey(key);
		if (object != null && object.getType() == COSObjType.COS_DICT) {
			return new PDAction(object);
		}
		return null;
	}

	public PDNavigationNode getNext() {
		return getNavidationNode(ASAtom.NEXT);
	}

	public PDNavigationNode getPrev() {
		return getNavidationNode(ASAtom.PREV);
	}

	private PDNavigationNode getNavidationNode(ASAtom key) {
		COSObject object = getKey(key);
		if (object != null && object.getType() == COSObjType.COS_DICT) {
			return new PDNavigationNode(object);
		}
		return null;
	}
}
