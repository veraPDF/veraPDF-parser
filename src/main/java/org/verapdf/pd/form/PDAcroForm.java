/**
 * This file is part of veraPDF Parser, a module of the veraPDF project.
 * Copyright (c) 2015-2025, veraPDF Consortium <info@verapdf.org>
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
package org.verapdf.pd.form;

import org.verapdf.as.ASAtom;
import org.verapdf.cos.COSArray;
import org.verapdf.cos.COSObjType;
import org.verapdf.cos.COSObject;
import org.verapdf.pd.PDObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Maksim Bezrukov
 */
public class PDAcroForm extends PDObject {

	public PDAcroForm(COSObject obj) {
		super(obj);
	}

	public COSObject getNeedAppearances() {
		return getKey(ASAtom.NEED_APPEARANCES);
	}

	public List<PDFormField> getFields() {
		COSObject fields = getKey(ASAtom.FIELDS);
		if (fields != null && fields.getType() == COSObjType.COS_ARRAY) {
			List<PDFormField> res = new ArrayList<>();
			for (COSObject obj : (COSArray) fields.getDirectBase()) {
				if (obj != null && obj.getType().isDictionaryBased()) {
					res.add(PDFormField.createTypedFormField(obj));
				}
			}
			return Collections.unmodifiableList(res);
		}
		return Collections.emptyList();
	}
}
