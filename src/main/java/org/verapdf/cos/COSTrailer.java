package org.verapdf.cos;

import org.verapdf.as.ASAtom;
import org.verapdf.pd.PDObject;

/**
 * @author Timur Kamalov
 */
public class COSTrailer extends PDObject {

	public COSTrailer() {
		super();
		setObject(COSDictionary.construct(), false);
	}

	public Long getSize() {
		return getObject().getIntegerKey(ASAtom.SIZE);
	}

	public void setSize(final Long size) {
		if (getPrev() != null && getPrev() != 0) {
			final Long prevSize = getObject().getIntegerKey(ASAtom.SIZE);
			if (prevSize > size) {
				return;
			}
		}
		getObject().setIntegerKey(ASAtom.SIZE, size);
	}

	public Long getPrev() {
		return getObject().getIntegerKey(ASAtom.PREV);
	}

	public void setPrev(final Long prev) {
		if (prev != 0) {
			getObject().setIntegerKey(ASAtom.PREV, prev);
		} else {
			removeKey(ASAtom.PREV);
		}
	}

	public COSObject getRoot() {
		return getKey(ASAtom.ROOT);
	}

	public void setRoot(final COSObject root) {
		setKey(ASAtom.ROOT, root);
	}

	public COSObject getEncrypt() {
		return getKey(ASAtom.ENCRYPT);
	}

	public void setEncrypt(final COSObject encrypt) {
		setKey(ASAtom.ENCRYPT, encrypt);
	}

	public COSObject getInfo() {
		return getKey(ASAtom.INFO);
	}

	public void setInfo(final COSObject info) {
		setKey(ASAtom.INFO, info);
	}

	public COSObject getID() {
		return getKey(ASAtom.ID);
	}

	public void setID(final COSObject id) {
		getObject().setArrayKey(ASAtom.ID, id);
	}

}
