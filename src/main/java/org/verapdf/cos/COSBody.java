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

import java.util.*;

/**
 * @author Timur Kamalov
 */
public class COSBody {

	private Map<COSKey, COSObject> table;

	public COSBody() {
		this.table = new HashMap<>();
	}

	public List<COSObject> getAll() {
		return new ArrayList<>(table.values());
	}

	public COSObject get(final COSKey key) {
		COSObject value = this.table.get(key);
		return value != null ? value : new COSObject();
	}

	public void set(final COSKey key, final COSObject object) {
		table.put(key, object);
	}

	public COSKey getKeyForObject(COSObject obj) {
		if (obj.isIndirect()) {
			return obj.getObjectKey();
		} else {
			for (COSKey key : this.table.keySet()) {
				if (this.table.get(key) == obj) {
                    return key;
                }
			}
			return null;
		}
	}
}
