package org.verapdf.pd;

import org.verapdf.cos.COSObject;

/**
 * @author Timur Kamalov
 */
public class PDObject {

	private COSObject object;

	public PDObject() {
		this.object = new COSObject();
	}

	public PDObject(final COSObject obj) {
		setObject(obj);
	}

	public boolean empty() {
		return this.object.empty();
	}

	public void clear() {
		this.object.clear();
	}

	public void setObject(COSObject object) {
		setObject(object, true);
	}

	public void setObject(COSObject object, boolean update) {
		this.object = object;
		if (update) {
			updateFromObject();
		}
	}

	// VIRTUAL METHODS
	protected void updateFromObject() {}

}
