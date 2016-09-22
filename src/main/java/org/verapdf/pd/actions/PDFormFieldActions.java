package org.verapdf.pd.actions;

import org.verapdf.as.ASAtom;
import org.verapdf.cos.COSObject;

/**
 * @author Maksim Bezrukov
 */
public class PDFormFieldActions extends PDAbstractAdditionalActions {

	public PDFormFieldActions(COSObject obj) {
		super(obj);
	}

	public PDAction getK() {
		return getAction(ASAtom.K);
	}

	public PDAction getF() {
		return getAction(ASAtom.F);
	}

	public PDAction getV() {
		return getAction(ASAtom.V);
	}

	public PDAction getC() {
		return getAction(ASAtom.C);
	}
}
