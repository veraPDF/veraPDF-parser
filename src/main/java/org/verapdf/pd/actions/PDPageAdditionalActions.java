package org.verapdf.pd.actions;

import org.verapdf.as.ASAtom;
import org.verapdf.cos.COSObjType;
import org.verapdf.cos.COSObject;
import org.verapdf.pd.PDObject;

/**
 * @author Timur Kamalov
 */
public class PDPageAdditionalActions extends PDObject {

	public PDPageAdditionalActions(COSObject obj) {
		super(obj);
	}

	public PDAction getO() {
		COSObject o = getKey(ASAtom.getASAtom("O"));
		PDAction retval = null;
		if (o != null && o.getType() == COSObjType.COS_DICT) {
			retval = new PDAction(o);
		}
		return retval;
	}

	public PDAction getC() {
		COSObject c = getKey(ASAtom.getASAtom("C"));
		PDAction retval = null;
		if (c != null && c.getType() == COSObjType.COS_DICT) {
			retval = new PDAction(c);
		}
		return retval;
	}

}
