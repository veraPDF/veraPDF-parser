package org.verapdf.pd.images;

import org.verapdf.as.ASAtom;
import org.verapdf.cos.COSDictionary;
import org.verapdf.cos.COSObjType;
import org.verapdf.cos.COSObject;
import org.verapdf.pd.PDResource;

/**
 * @author Maksim Bezrukov
 */
public abstract class PDXObject extends PDResource {

	protected PDXObject(COSObject obj) {
		super(obj);
	}

	public abstract ASAtom getType();

	public ASAtom getSubtype() {
		return getObject().getNameKey(ASAtom.SUBTYPE);
	}

	public COSDictionary getOPI() {
		COSObject opi = getKey(ASAtom.OPI);
		if (opi != null && opi.getType() == COSObjType.COS_DICT) {
			return (COSDictionary) opi.getDirectBase();
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

	public static PDXObject getTypedPDXObject(COSObject object) {
		ASAtom type = object.getNameKey(ASAtom.SUBTYPE);
		if (ASAtom.IMAGE.equals(type)) {
			return new PDXImage(object);
		} else if (ASAtom.FORM.equals(type)) {
			if (ASAtom.PS.equals(object.getNameKey(ASAtom.SUBTYPE_2))) {
				return new PDXPostScript(object);
			} else {
				return new PDXForm(object);
			}
		} else if (ASAtom.PS.equals(type)) {
			return new PDXPostScript(object);
		} else {
			return null;
		}
	}
}
