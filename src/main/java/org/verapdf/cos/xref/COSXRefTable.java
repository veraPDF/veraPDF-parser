/**
 * This file is part of veraPDF Parser, a module of the veraPDF project.
 * Copyright (c) 2015-2024, veraPDF Consortium <info@verapdf.org>
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
package org.verapdf.cos.xref;

import org.verapdf.cos.COSKey;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Timur Kamalov
 */
public class COSXRefTable {

	private List<COSKey> all;
	private int maxKeyNumber;

	public COSXRefTable() {
		this.all = new ArrayList<>();
		maxKeyNumber = 0;
	}

	public void set(final List<COSKey> keys) {
		this.all = keys;
		maxKeyNumber = keys.stream().map(COSKey::getNumber).max(Integer::compare).orElse(0);
	}

	private int getGreatestKeyNumberFromXref() {
		return maxKeyNumber;
	}

	public COSKey next() {
		return new COSKey(getGreatestKeyNumberFromXref() + 1);
	}

	public void newKey(final COSKey key) {
		this.all.add(key);
		if (key.getNumber() > maxKeyNumber) {
			maxKeyNumber = key.getNumber();
		}
	}

	public void newKey(final List<COSKey> key) {
		this.all.addAll(key);
		int newKeysMaxKeyNumber = key.stream().map(COSKey::getNumber).max(Integer::compare).orElse(0);
		if (newKeysMaxKeyNumber > maxKeyNumber) {
			maxKeyNumber = newKeysMaxKeyNumber;
		}
	}

	public List<COSKey> getAllKeys() {
		return this.all;
	}
}