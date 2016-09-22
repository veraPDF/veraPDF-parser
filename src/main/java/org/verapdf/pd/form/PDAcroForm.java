package org.verapdf.pd.form;

import org.verapdf.as.ASAtom;
import org.verapdf.cos.COSArray;
import org.verapdf.cos.COSObjType;
import org.verapdf.cos.COSObject;
import org.verapdf.pd.PDObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Maksim Bezrukov
 */
public class PDAcroForm extends PDObject {

	public PDAcroForm(COSObject obj) {
		super(obj);
	}

	public Boolean getNeedAppearances() {
		return getBooleanKey(ASAtom.NEED_APPEARANCES);
	}

	public List<PDFormField> getFields() {
		COSObject fields = getKey(ASAtom.FIELDS);
		if (fields != null && fields.getType() == COSObjType.COS_ARRAY) {
			List<PDFormField> res = new ArrayList<>();
			for (COSObject obj : (COSArray) fields.getDirectBase()) {
				if (obj != null && obj.getType().isDictionaryBased()) {
					res.add(PDFormField.createTypedFormField(obj));
				}
			}
			return Collections.unmodifiableList(res);
		}
		return Collections.emptyList();
	}
}
