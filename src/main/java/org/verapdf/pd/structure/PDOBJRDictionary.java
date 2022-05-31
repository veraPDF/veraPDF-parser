package org.verapdf.pd.structure;

import org.verapdf.as.ASAtom;
import org.verapdf.cos.COSKey;
import org.verapdf.cos.COSObject;
import org.verapdf.pd.PDObject;


public class PDOBJRDictionary extends PDObject {

	public PDOBJRDictionary(COSObject cosObject) {
		super(cosObject);
	}

	public COSKey getPageObjectKey() {
		COSObject object = getObject().getKey(ASAtom.PG);
		return object != null ? object.getKey() : null;
	}

	public COSObject getReferencedObject() {
		return getObject().getKey(ASAtom.OBJ);
	}
}
