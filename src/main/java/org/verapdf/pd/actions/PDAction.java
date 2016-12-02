package org.verapdf.pd.actions;

import org.verapdf.as.ASAtom;
import org.verapdf.cos.COSArray;
import org.verapdf.cos.COSNumber;
import org.verapdf.cos.COSObjType;
import org.verapdf.cos.COSObject;
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
}
