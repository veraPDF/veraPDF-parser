/**
 * This file is part of veraPDF Parser, a module of the veraPDF project.
 * Copyright (c) 2015, veraPDF Consortium <info@verapdf.org>
 * All rights reserved.
 *
 * veraPDF Parser is free software: you can redistribute it and/or modify
 * it under the terms of either:
 *
 * The GNU General public license GPLv3+.
 * You should have received a copy of the GNU General Public License
 * along with veraPDF Parser as the LICENSE.GPL file in the root of the source
 * tree.  If not, see http://www.gnu.org/licenses/ or
 * https://www.gnu.org/licenses/gpl-3.0.en.html.
 *
 * The Mozilla Public License MPLv2+.
 * You should have received a copy of the Mozilla Public License along with
 * veraPDF Parser as the LICENSE.MPL file in the root of the source tree.
 * If a copy of the MPL was not distributed with this file, you can obtain one at
 * http://mozilla.org/MPL/2.0/.
 */
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

	public Long getXRefStm() {
		return getObject().getIntegerKey(ASAtom.XREF_STM);
	}

	public void setXRefStm(final Long prev) {
		if (prev != 0) {
			getObject().setIntegerKey(ASAtom.XREF_STM, prev);
		} else {
			removeKey(ASAtom.XREF_STM);
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
