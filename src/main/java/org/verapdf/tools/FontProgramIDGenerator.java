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
				+ " " + getCOSObjectID(cidToGIDMap);
	}

	public static String getCFFFontProgramID(COSKey key, CMap cMap, boolean isSubset) {
		return getBaseFontProgramID("CFFFontProgram", key, cMap)
				+ " " + String.valueOf(isSubset);
	}

	public static String getOpenTypeFontProgramID(COSKey key, boolean isCFF, boolean isSymbolic, COSObject encoding, CMap cMap, boolean isSubset) {
		return getBaseFontProgramID("OpenTypeFontProgram", key, cMap)
				+ " " + String.valueOf(isCFF)
				+ " " + String.valueOf(isSymbolic)
				+ " " + getCOSObjectID(encoding)
				+ " " + String.valueOf(isSubset);
	}

	public static String getTrueTypeFontProgramID(COSKey key, boolean isSymbolic, COSObject encoding) {
		if (key == null) {
			return null;
		}
		return "TrueTypeFontProgram " + key.toString()
				+ " " + String.valueOf(isSymbolic)
				+ " " + getCOSObjectID(encoding);
	}

	public static String getType1FontProgramID(COSKey key) {
		if (key == null) {
			return null;
		}
		return "Type1FontProgram " + key.toString();
	}

	private static String getBaseFontProgramID(String type, COSKey key, CMap cMap) {
		if (key == null) {
			return null;
		}
		return type + " " + key.toString() + " " + getObjectID(cMap);
	}

	private static String getCOSObjectID(COSObject cosObject) {
		if (cosObject != null) {
			COSKey mapObjectKey = cosObject.getObjectKey();
			if (mapObjectKey != null) {
				return mapObjectKey.toString();
			} else {
				return String.valueOf(cosObject.hashCode());
			}
		}
		return NULL;
	}

	private static String getObjectID(Object obj) {
		return obj == null ? NULL : String.valueOf(obj.hashCode());
	}
}
