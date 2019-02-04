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
package org.verapdf.pd.images;

import org.verapdf.as.ASAtom;
import org.verapdf.cos.COSDictionary;
import org.verapdf.cos.COSObjType;
import org.verapdf.cos.COSObject;
import org.verapdf.pd.PDResource;
import org.verapdf.pd.PDResources;

/**
 * @author Maksim Bezrukov
 */
public abstract class PDXObject extends PDResource {

	protected PDXObject(COSObject obj) {
		super(obj);
	}

	public abstract ASAtom getType();

	public ASAtom getSubtype() {
		return getObject().getNameKey(ASAtom.SUBTYPE);
	}

	public COSDictionary getOPI() {
		COSObject opi = getKey(ASAtom.OPI);
		if (opi != null && opi.getType() == COSObjType.COS_DICT) {
			return (COSDictionary) opi.getDirectBase();
		}
		return null;
	}

	public PDXImage getSMask() {
		COSObject smask = getKey(ASAtom.SMASK);
		if (smask != null && smask.getType() == COSObjType.COS_STREAM) {
			return new PDXImage(smask, null);
			// TODO: see pdfbox PBoxPDXObject getSMask() and getXObject(COSBase smaskDictionary) methods.
			// Implement everything nicely.
		}
		return null;
	}

	public static PDXObject getTypedPDXObject(COSObject object, PDResources resources) {
		if (object != null && !object.empty()) {
			ASAtom type = object.getNameKey(ASAtom.SUBTYPE);
			if (ASAtom.IMAGE.equals(type)) {
				return new PDXImage(object, resources);
			} else if (ASAtom.FORM.equals(type)) {
				return new PDXForm(object);
			} else if (ASAtom.PS.equals(type)) {
				return new PDXPostScript(object);
			}
		}
		return null;
	}
}
