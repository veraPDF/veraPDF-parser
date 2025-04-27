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
package org.verapdf.pd;

import org.verapdf.as.ASAtom;
import org.verapdf.cos.COSObjType;
import org.verapdf.cos.COSObject;
import org.verapdf.pd.actions.PDAction;

/**
 * @author Maksim Bezrukov
 */
public class PDNavigationNode extends PDObject {

	public PDNavigationNode(COSObject obj) {
		super(obj);
	}

	public PDAction getNA() {
		return getAction(ASAtom.NA);
	}

	public PDAction getPA() {
		return getAction(ASAtom.PA);
	}

	private PDAction getAction(ASAtom key) {
		COSObject object = getKey(key);
		if (object != null && object.getType() == COSObjType.COS_DICT) {
			return new PDAction(object);
		}
		return null;
	}

	public PDNavigationNode getNext() {
		return getNavigationNode(ASAtom.NEXT);
	}

	public PDNavigationNode getPrev() {
		return getNavigationNode(ASAtom.PREV);
	}

	private PDNavigationNode getNavigationNode(ASAtom key) {
		COSObject object = getKey(key);
		if (object != null && object.getType() == COSObjType.COS_DICT) {
			return new PDNavigationNode(object);
		}
		return null;
	}
}
