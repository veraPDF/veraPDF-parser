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
			if (obj.getType() == COSObjType.COS_STREAM) {
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
