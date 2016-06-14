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
		this.setObject(obj);
	}

	public boolean empty() {
		return object.empty();
	}

	public void clear() {
		object.clear();
	}

	public COSObject getObject() {
		return object;
	}

	public void setObject(final COSObject object) {
		this.setObject(object, true);
	}

	public void setObject(final COSObject object, final boolean update) {
		this.object = object;
		if (update) {
			updateFromObject();
		}
	}

	public boolean knownKey(final ASAtom key) {
		return object.knownKey(key);
	}

	public COSObject getKey(final ASAtom key) {
		return object.getKey(key);
	}

	public void setKey(final ASAtom key, final COSObject value) {
		object.setKey(key, value);
	}

	public void removeKey(final ASAtom key) {
		object.removeKey(key);
	}

	// VIRTUAL METHODS
	protected void updateToObject() {}
	protected void updateFromObject() {}

}
