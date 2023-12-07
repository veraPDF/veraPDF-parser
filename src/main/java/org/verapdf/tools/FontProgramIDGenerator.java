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
package org.verapdf.tools;

import org.verapdf.cos.COSKey;
import org.verapdf.cos.COSObject;
import org.verapdf.pd.font.cmap.CMap;

/**
 * @author Maksim Bezrukov
 */
// TODO: remove after refactor. Currently we want to cache font programs as PD objects.
// We can use COSKey for cache key, but for that we have to remove all objects which are not
// related to specified font program from its constructor. For example CMaps in CIDFontType2 object
public class FontProgramIDGenerator {

	private static final String NULL = "null";

	private FontProgramIDGenerator() {
	}

	public static String getCIDFontType2ProgramID(COSKey key, CMap cMap, COSObject cidToGIDMap) {
		return getBaseFontProgramID("CIDFontType2Program", key, cMap)
				+ ' ' + getCOSObjectID(cidToGIDMap);
	}

	public static String getCFFFontProgramID(COSKey key, CMap cMap, boolean isSubset) {
		return getBaseFontProgramID("CFFFontProgram", key, cMap)
				+ ' ' + isSubset;
	}

	public static String getOpenTypeFontProgramID(COSKey key, boolean isCFF, boolean isSymbolic, COSObject encoding, CMap cMap, boolean isSubset) {
		return getBaseFontProgramID("OpenTypeFontProgram", key, cMap)
				+ ' ' + isCFF
				+ ' ' + isSymbolic
				+ ' ' + getCOSObjectID(encoding)
				+ ' ' + isSubset;
	}

	public static String getTrueTypeFontProgramID(COSKey key, boolean isSymbolic, COSObject encoding) {
		if (key == null) {
			return null;
		}
		return "TrueTypeFontProgram " + key
				+ ' ' + isSymbolic
				+ ' ' + getCOSObjectID(encoding);
	}

	public static String getType1FontProgramID(COSKey key) {
		if (key == null) {
			return null;
		}
		return "Type1FontProgram " + key;
	}

	private static String getBaseFontProgramID(String type, COSKey key, CMap cMap) {
		if (key == null) {
			return null;
		}
		return type + ' ' + key + ' ' + getObjectID(cMap);
	}

	private static String getCOSObjectID(COSObject cosObject) {
		if (cosObject != null) {
			COSKey mapObjectKey = cosObject.getObjectKey();
			if (mapObjectKey != null) {
				return mapObjectKey.toString();
			} else {
				return "direct";
			}
		}
		return NULL;
	}

	private static String getObjectID(Object obj) {
		return obj == null ? NULL : String.valueOf(obj.hashCode());
	}
}
