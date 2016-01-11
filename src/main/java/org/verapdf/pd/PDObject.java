package org.verapdf.pd;

import org.verapdf.as.ASAtom;
import org.verapdf.cos.COSObject;

import java.io.IOException;

/**
 * @author Timur Kamalov
 */
public class PDObject {

	private COSObject object;

	public PDObject() {
		this.object = new COSObject();
	}

	public PDObject(final COSObject obj) throws Exception{
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

	public void setObject(COSObject object) throws Exception {
		setObject(object, true);
	}

	public void setObject(COSObject object, boolean update) throws Exception {
		this.object = object;
		if (update) {
			updateFromObject();
		}
	}

	public boolean knownKey(final ASAtom key) throws IOException {
		return this.object.knownKey(key);
	}

	public COSObject getKey(final ASAtom key) throws IOException {
		return this.object.getKey(key);
	}

	public void setKey(final ASAtom key, final COSObject value) throws IOException {
		this.object.setKey(key, value);
	}

	public void removeKey(final ASAtom key) throws IOException {
		this.object.removeKey(key);
	}

	// VIRTUAL METHODS
	protected void updateToObject() throws IOException {}
	protected void updateFromObject() throws Exception {}

}
