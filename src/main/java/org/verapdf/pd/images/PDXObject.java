package org.verapdf.pd.images;

import org.verapdf.as.ASAtom;
import org.verapdf.cos.COSDictionary;
import org.verapdf.cos.COSObjType;
import org.verapdf.cos.COSObject;
import org.verapdf.pd.PDResource;

/**
 * @author Maksim Bezrukov
 */
public class PDXObject extends PDResource {

	protected PDXObject(COSObject obj) {
		super(obj);
	}

	public ASAtom getSubtype() {
		return getObject().getNameKey(ASAtom.SUBTYPE);
	}

	public COSDictionary getOPI() {
		COSObject opi = getKey(ASAtom.OPI);
		if (opi != null && opi.getType() == COSObjType.COS_DICT) {
			return (COSDictionary) opi.get();
		}
		return null;
	}

	public PDXImage getSMask() {
		COSObject smask = getKey(ASAtom.SMASK);
		if (smask != null && smask.getType() == COSObjType.COS_STREAM) {
			return new PDXImage(smask);
		}
		return null;
	}
}
