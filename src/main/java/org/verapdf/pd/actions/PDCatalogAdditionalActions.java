package org.verapdf.pd.actions;

import org.verapdf.as.ASAtom;
import org.verapdf.cos.COSObject;

/**
 * @author Timur Kamalov
 */
public class PDCatalogAdditionalActions extends PDAbstractAdditionalActions {

	public PDCatalogAdditionalActions(COSObject obj) {
		super(obj);
	}

	public PDAction getWC() {
		return getAction(ASAtom.WC);
	}

	public PDAction getWS() {
		return getAction(ASAtom.WS);
	}

	public PDAction getDS() {
		return getAction(ASAtom.DS);
	}

	public PDAction getWP() {
		return getAction(ASAtom.WP);
	}

	public PDAction getDP() {
		return getAction(ASAtom.DP);
	}
}
