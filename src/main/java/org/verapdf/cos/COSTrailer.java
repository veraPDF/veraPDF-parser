package org.verapdf.cos;

import org.verapdf.as.ASAtom;
import org.verapdf.pd.PDObject;

import java.io.IOException;

/**
 * @author Timur Kamalov
 */
public class COSTrailer extends PDObject {

	public COSTrailer() throws Exception {
		super();
		setObject(COSDictionary.construct(), false);
	}

	public long getSize() throws IOException {
		return getObject().getIntegerKey(ASAtom.SIZE);
	}

	public void setSize(final long size) throws IOException {
		if (getPrev() != 0) {
			final long prevSize = getObject().getIntegerKey(ASAtom.SIZE);
			if (prevSize > size) {
				return;
			}
		}
		getObject().setIntegerKey(ASAtom.SIZE, size);
	}

	public long getPrev() throws IOException {
		return getObject().getIntegerKey(ASAtom.PREV);
	}

	public void setPrev(final long prev) throws IOException {
		if (prev != 0) {
			getObject().setIntegerKey(ASAtom.PREV, prev);
		} else {
			removeKey(ASAtom.PREV);
		}
	}

	public COSObject getRoot() throws IOException {
		return getKey(ASAtom.ROOT);
	}

	public void setRoot(final COSObject root) throws IOException {
		setKey(ASAtom.ROOT, root);
	}

	public COSObject getEncrypt() throws IOException {
		return getKey(ASAtom.ENCRYPT);
	}

	public void setEncrypt(final COSObject encrypt) throws IOException {
		setKey(ASAtom.ENCRYPT, encrypt);
	}

	public COSObject getInfo() throws IOException {
		return getKey(ASAtom.INFO);
	}

	public void setInfo(final COSObject info) throws IOException {
		setKey(ASAtom.INFO, info);
	}

	public COSObject getID() throws IOException {
		return getKey(ASAtom.ID);
	}

	public void setID(final COSObject id) throws IOException {
		COSObject[] ids = new COSObject[2];

		ids[0] = getObject().getKey(ASAtom.ID).at(0);
		if (ids[0].empty()) {
			ids[0] = id;
		}
		ids[1] = id;

		getObject().setArrayKey(ASAtom.ID, 2, ids);
	}

}
