package org.verapdf.pd;

import org.verapdf.as.ASAtom;
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

	public COSObject getObject() {
		return this.object;
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

	public boolean knownKey(final ASAtom key) {
		return this.object.knownKey(key);
	}

	public COSObject getKey(final ASAtom key) {
		return this.object.getKey(key);
	}

	public void setKey(final ASAtom key, final COSObject value) {
		this.object.setKey(key, value);
	}

	public void removeKey(final ASAtom key) {
		this.object.removeKey(key);
	}

	// VIRTUAL METHODS
	protected void updateToObject() {}
	protected void updateFromObject() {}

}
