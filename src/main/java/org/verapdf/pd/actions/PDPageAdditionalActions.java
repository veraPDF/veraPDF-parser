package org.verapdf.pd.actions;

import org.verapdf.as.ASAtom;
import org.verapdf.cos.COSObject;

/**
 * @author Timur Kamalov
 */
public class PDPageAdditionalActions extends PDAbstractAdditionalActions {

	public PDPageAdditionalActions(COSObject obj) {
		super(obj);
	}

	public PDAction getO() {
		return getAction(ASAtom.O);
	}

	public PDAction getC() {
		return getAction(ASAtom.C);
	}

}
