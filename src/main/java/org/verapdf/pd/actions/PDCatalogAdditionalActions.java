package org.verapdf.pd.actions;

import org.verapdf.as.ASAtom;
import org.verapdf.cos.COSObjType;
import org.verapdf.cos.COSObject;
import org.verapdf.pd.PDObject;

/**
 * @author Timur Kamalov
 */
public class PDCatalogAdditionalActions extends PDObject {

	public PDCatalogAdditionalActions(COSObject obj) {
		super(obj);
	}

	public PDAction getWC() {
		COSObject wc = getKey(ASAtom.getASAtom("WC"));
		PDAction retval = null;
		if (wc != null && wc.getType() == COSObjType.COS_DICT) {
			retval = new PDAction(wc);
		}
		return retval;
	}

	public PDAction getWS() {
		COSObject ws = getKey(ASAtom.getASAtom("WS"));
		PDAction retval = null;
		if (ws != null && ws.getType() == COSObjType.COS_DICT) {
			retval = new PDAction(ws);
		}
		return retval;
	}

	public PDAction getDS() {
		COSObject ds = getKey(ASAtom.getASAtom("DS"));
		PDAction retval = null;
		if (ds != null && ds.getType() == COSObjType.COS_DICT) {
			retval = new PDAction(ds);
		}
		return retval;
	}

	public PDAction getWP() {
		COSObject wp = getKey(ASAtom.getASAtom("WP"));
		PDAction retval = null;
		if (wp != null && wp.getType() == COSObjType.COS_DICT) {
			retval = new PDAction(wp);
		}
		return retval;
	}

	public PDAction getDP() {
		COSObject dp = getKey(ASAtom.getASAtom("DP"));
		PDAction retval = null;
		if (dp != null && dp.getType() == COSObjType.COS_DICT) {
			retval = new PDAction(dp);
		}
		return retval;
	}

}
