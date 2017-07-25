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
		this.setObject(obj == null ? COSObject.getEmpty() : obj);
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

	public String getStringKey(final ASAtom key) {
		return object.getStringKey(key);
	}

	public void setStringKey(final ASAtom key, final String value) {
		object.setStringKey(key, value);
	}

	public ASAtom getNameKey(final ASAtom key) {
		return object.getNameKey(key);
	}

	public void setNameKey(final ASAtom key, final ASAtom value) {
		object.setNameKey(key, value);
	}

	public Long getIntegerKey(final ASAtom key) {
		return object.getIntegerKey(key);
	}

	public Double getRealKey(final ASAtom key) {
		return object.getRealKey(key);
	}

	public void setIntegerKey(final ASAtom key, final Long value) {
		object.setIntegerKey(key, value);
	}

	public Boolean getBooleanKey(final ASAtom key) {
		return object.getBooleanKey(key);
	}

	public void setBooleanKey(final ASAtom key, final Boolean value) {
		object.setBooleanKey(key, value);
	}

	public void removeKey(final ASAtom key) {
		object.removeKey(key);
	}

	// VIRTUAL METHODS
	protected void updateToObject() {}
	protected void updateFromObject() {}

}
