package org.verapdf.pd.form;

import org.verapdf.as.ASAtom;
import org.verapdf.cos.COSArray;
import org.verapdf.cos.COSKey;
import org.verapdf.cos.COSObjType;
import org.verapdf.cos.COSObject;
import org.verapdf.pd.PDObject;
import org.verapdf.pd.actions.PDFormFieldActions;

import java.util.*;

/**
 * @author Maksim Bezrukov
 */
public class PDFormField extends PDObject {

	protected PDFormField(COSObject obj) {
		super(obj);
	}

    public static PDFormField createTypedFormField(COSObject obj) {
        ASAtom fieldType = getFieldTypeCOSObject(obj);
        if (fieldType == ASAtom.SIG) {
            return new PDSignatureField(obj);
        }
        return new PDFormField(obj);
    }

	public ASAtom getFT() {
		return getInheritedFT(getObject());
	}

	private static ASAtom getInheritedFT(COSObject obj) {
		COSObject currObject = obj;
		Set<COSKey> checkedObjects = new HashSet<>();
		while (currObject != null) {
			ASAtom currFT = currObject.getNameKey(ASAtom.FT);
			if (currFT != null) {
				return currFT;
			}

			COSKey currKey = currObject.getKey();
			if (currKey != null) {
				checkedObjects.add(currKey);
			}

			COSObject parent = currObject.getKey(ASAtom.PARENT);
			if (parent != null
					&& parent.getType().isDictionaryBased()
					&& !checkedObjects.contains(parent.getKey())) {
				currObject = parent;
			} else {
				currObject = null;
			}
		}
		return null;
	}

	public PDFormFieldActions getActions() {
		COSObject object = getKey(ASAtom.AA);
		if (object != null && object.getType().isDictionaryBased()) {
			return new PDFormFieldActions(object);
		}
		return null;
	}

	public List<PDFormField> getChildFormFields() {
		if (isNonTerminalField()) {
			List<PDFormField> res = new ArrayList<>();
			for (COSObject elem : (COSArray) getKey(ASAtom.KIDS).getDirectBase()) {
				res.add(new PDFormField(elem));
			}
			return Collections.unmodifiableList(res);
		}
		return Collections.emptyList();
	}

	private boolean isNonTerminalField() {
		COSObject kids = getKey(ASAtom.KIDS);
		if (kids != null && kids.getType() == COSObjType.COS_ARRAY) {
			for (COSObject elem : (COSArray) kids.getDirectBase()) {
				if (elem == null
						|| !elem.getType().isDictionaryBased()
						|| ASAtom.ANNOT.equals(elem.getNameKey(ASAtom.TYPE))) {
					return false;
				}
			}
			return true;
		}
		return false;
	}

    private static ASAtom getFieldTypeCOSObject(COSObject field) {
        ASAtom res = field.getNameKey(ASAtom.FT);
        if (res != null) {
            return res;
        }
        COSObject parent = field.getKey(ASAtom.PARENT);
        if (parent != null) {
            return getFieldTypeCOSObject(parent);
        }
        return null;
    }
}
