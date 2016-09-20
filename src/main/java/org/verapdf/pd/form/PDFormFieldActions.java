package org.verapdf.pd.form;

import org.verapdf.as.ASAtom;
import org.verapdf.cos.COSObject;
import org.verapdf.pd.PDObject;
import org.verapdf.pd.actions.PDAction;

/**
 * @author Maksim Bezrukov
 */
public class PDFormFieldActions extends PDObject {

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

	private PDAction getAction(ASAtom key) {
		COSObject obj = getKey(key);
		if (obj != null && obj.getType().isDictionaryBased()) {
			return new PDAction(obj);
		}
		return null;
	}
}
