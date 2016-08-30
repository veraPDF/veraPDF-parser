package org.verapdf.pd;

import org.verapdf.as.ASAtom;
import org.verapdf.cos.COSObject;
import org.verapdf.factory.colors.ColorSpaceFactory;
import org.verapdf.pd.colors.PDColorSpace;

/**
 * @author Maksim Bezrukov
 */
public class PDGroup extends PDObject {

	public PDGroup(COSObject obj) {
		super(obj);
	}

	public ASAtom getSubtype() {
		return getObject().getNameKey(ASAtom.S);
	}

	public PDColorSpace getColorSpace() {
		return ColorSpaceFactory.getColorSpace(getKey(ASAtom.CS));
	}

	public boolean isIsolated() {
		Boolean value = getObject().getBooleanKey(ASAtom.I);
		return value != null ? value.booleanValue() : false;
	}

	public boolean isKnockout() {
		Boolean value = getObject().getBooleanKey(ASAtom.K);
		return value != null ? value.booleanValue() : false;
	}
}
