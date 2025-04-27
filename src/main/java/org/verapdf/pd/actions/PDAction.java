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
package org.verapdf.pd.actions;

import org.verapdf.as.ASAtom;
import org.verapdf.cos.COSArray;
import org.verapdf.cos.COSNumber;
import org.verapdf.cos.COSObjType;
import org.verapdf.cos.COSObject;
import org.verapdf.pd.PDDestination;
import org.verapdf.pd.PDObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Maksim Bezrukov
 */
public class PDAction extends PDObject {
	public PDAction(COSObject obj) {
		super(obj);
	}

	public ASAtom getSubtype() {
		return getObject().getNameKey(ASAtom.S);
	}

	public List<PDAction> getNext() {
		COSObject next = getKey(ASAtom.NEXT);
		if (next != null) {
			COSObjType type = next.getType();
			List<PDAction> actions = new ArrayList<>();
			if (type == COSObjType.COS_DICT) {
				actions.add(new PDAction(next));
			} else if (type == COSObjType.COS_ARRAY) {
				for (COSObject obj : (COSArray) next.getDirectBase()) {
					if (obj != null && obj.getType() == COSObjType.COS_DICT) {
						actions.add(new PDAction(obj));
					}
				}
			}
			return Collections.unmodifiableList(actions);
		}
		return Collections.emptyList();
	}

	public List<COSNumber> getCOSArrayD() {
		COSObject d = getKey(ASAtom.D);
		if (d != null && d.getType() == COSObjType.COS_ARRAY) {
			List<COSNumber> numbers = new ArrayList<>();
			for (COSObject obj : (COSArray) d.getDirectBase()) {
				if (obj != null && obj.getType().isNumber()) {
					numbers.add((COSNumber) obj.getDirectBase());
				}
			}
			return Collections.unmodifiableList(numbers);
		}
		return Collections.emptyList();
	}

	public ASAtom getN() {
		return getObject().getNameKey(ASAtom.N);
	}

	public COSObject getRendition() {
		return getObject().getKey(ASAtom.R);
	}

	public COSObject getDestination() {
		return getObject().getKey(ASAtom.D);
	}

	public COSObject getStructureDestination() {
		return getObject().getKey(ASAtom.SD);
	}

	public boolean containsStructureDestination() {
		if (getObject().knownKey(ASAtom.SD)) {
			return true;
		}
		if (getObject().knownKey(ASAtom.D)) {
			COSObject d = getDestination();
			if (d.getType() == COSObjType.COS_NAME || d.getType() == COSObjType.COS_STRING) {
				return new PDDestination(d).getIsStructDestination();
			}
		}
		return false;
	}
}
