package org.verapdf.pd;

import org.verapdf.as.ASAtom;
import org.verapdf.cos.COSName;
import org.verapdf.cos.COSObjType;
import org.verapdf.cos.COSObject;
import org.verapdf.tools.TaggedPDFHelper;

import java.util.List;

/**
 * @author Maksim Bezrukov
 */
public class PDStructElem extends PDObject {

	public PDStructElem(COSObject obj) {
		super(obj);
	}

	public ASAtom getType() {
		return getObject().getNameKey(ASAtom.TYPE);
	}

	public ASAtom getStructureType() {
		return getObject().getNameKey(ASAtom.S);
	}

	public COSName getCOSStructureType() {
		COSObject object = getKey(ASAtom.S);
		if (object != null && object.getType() == COSObjType.COS_NAME) {
			return (COSName) object.get();
		}
		return null;
	}

	public String getLang() {
		return getObject().getStringKey(ASAtom.LANG);
	}

	public List<PDStructElem> getChildren() {
		return TaggedPDFHelper.getStructElemChildren(getObject());
	}
}
