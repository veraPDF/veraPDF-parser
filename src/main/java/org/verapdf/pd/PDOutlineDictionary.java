package org.verapdf.pd;

import org.verapdf.as.ASAtom;
import org.verapdf.cos.COSObject;

/**
 * @author Maksim Bezrukov
 */
public class PDOutlineDictionary extends PDObject {

	public PDOutlineDictionary(COSObject obj) {
		super(obj);
	}

	public PDOutlineItem getFirst() {
		return getOutlineItem(ASAtom.FIRST);
	}

	public PDOutlineItem getLast() {
		return getOutlineItem(ASAtom.LAST);
	}

	protected PDOutlineItem getOutlineItem(ASAtom key) {
		COSObject first = getKey(key);
		if (first != null && first.getType().isDictionaryBased()) {
			return new PDOutlineItem(first);
		}
		return null;
	}
}
