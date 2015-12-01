package org.verapdf.pd;

import org.verapdf.cos.COSObject;

/**
 * @author Timur Kamalov
 */
public class PDObject {

	private COSObject cosObject;

	public PDObject() {
	}

	public PDObject(final COSObject obj) {
	}

	public COSObject getCosObject() {
		return cosObject;
	}

	public void setCosObject(COSObject cosObject, boolean update) {
		this.cosObject = cosObject;
	}

	public void clear() {
		this.cosObject.clear();
	}
}
