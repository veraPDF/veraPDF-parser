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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Maksim Bezrukov
 */
public class PDAppearanceEntry extends PDObject {

	public PDAppearanceEntry(COSObject obj) {
		super(obj);
	}

	public boolean isSubDictionary() {
		return getObject().getType() == COSObjType.COS_DICT;
	}

	public Map<ASAtom, PDAppearanceStream> getSubDictionary() {
		if (!isSubDictionary()) {
			throw new IllegalStateException("Current appearance entry is a stream");
		}

		Map<ASAtom, PDAppearanceStream> res = new HashMap<>();
		for (ASAtom key : getObject().getKeySet()) {
			COSObject obj = getKey(key);
			if (obj.isIndirect()) {
				obj = obj.getDirect();
			}
			if (obj != null && obj.getType() == COSObjType.COS_STREAM) {
				res.put(key, new PDAppearanceStream(obj));
			}
		}
		return Collections.unmodifiableMap(res);
	}

	public PDAppearanceStream getAppearanceStream() {
		if (isSubDictionary()) {
			throw new IllegalStateException("Current appearance entry is not a stream");
		}
		return new PDAppearanceStream(getObject());
	}
}
