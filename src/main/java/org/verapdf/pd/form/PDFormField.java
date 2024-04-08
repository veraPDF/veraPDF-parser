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
package org.verapdf.pd.form;

import org.verapdf.as.ASAtom;
import org.verapdf.cos.COSArray;
import org.verapdf.cos.COSKey;
import org.verapdf.cos.COSObjType;
import org.verapdf.cos.COSObject;
import org.verapdf.exceptions.LoopedException;
import org.verapdf.pd.PDObject;
import org.verapdf.pd.actions.PDFormFieldActions;

import java.util.*;

/**
 * @author Maksim Bezrukov
 */
public class PDFormField extends PDObject {

	private final Set<COSKey> parents;

	protected PDFormField(COSObject obj, Set<COSKey> parents) {
		super(obj);
		COSKey objectKey = obj.getObjectKey();
		this.parents = new HashSet<>(parents);
		if (objectKey != null) {
			if (this.parents.contains(objectKey)) {
				throw new LoopedException("Loop form field tree");
			} else {
				this.parents.add(objectKey);
			}
		}
	}

	public static PDFormField createTypedFormField(COSObject obj) {
		return createTypedFormField(obj, new HashSet<>());
	}

    private static PDFormField createTypedFormField(COSObject obj, Set<COSKey> parents) {
		if (obj == null) {
			throw new IllegalArgumentException("Argument object can not be null");
		}
		if (parents == null) {
			throw new IllegalArgumentException("Argument parents can not be null");
		}
        ASAtom fieldType = getFieldTypeCOSObject(obj);
        if (fieldType == ASAtom.SIG) {
            return new PDSignatureField(obj, parents);
        }
        return new PDFormField(obj, parents);
    }

	public ASAtom getFT() {
		COSObject object = getInheritedObject(ASAtom.FT);
		return object == null || object.getType() != COSObjType.COS_NAME ? null : object.getName();
	}

	public COSObject getV() {
		return getInheritedObject(ASAtom.V);
	}

	private COSObject getInheritedObject(ASAtom key) {
		COSObject currObject = getObject();
		Set<COSKey> checkedObjects = new HashSet<>();
		while (currObject != null) {
			COSObject currVal = currObject.getKey(key);
			if (currVal != null && !currVal.empty()) {
				return currVal;
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

	public String getFullyQualifiedName() {
		List<String> parts = new ArrayList<>();
		COSObject currObject = getObject();
		Set<COSKey> checkedObjects = new HashSet<>();
		while (currObject != null && !currObject.empty()) {
			String partial = currObject.getStringKey(ASAtom.T);
			if (partial != null) {
				parts.add(partial);
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
		if (!parts.isEmpty()) {
			StringBuilder builder = new StringBuilder();
			for (int i = parts.size() - 1; i > 0; --i) {
				builder.append(parts.get(i)).append('.');
			}
			builder.append(parts.get(0));
			return builder.toString();
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
		COSObject kids = getKey(ASAtom.KIDS);
		if (kids != null && kids.getType() == COSObjType.COS_ARRAY) {
			List<PDFormField> res = new ArrayList<>();
			for (COSObject elem : (COSArray)kids.getDirectBase()) {
				if (isField(elem)) {
					res.add(createTypedFormField(elem, this.parents));
				}
			}
			return Collections.unmodifiableList(res);
		}
		return Collections.emptyList();
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

	public String getTU() {
		return getStringKey(ASAtom.TU);
	}

	public Long getFf() {
		return getIntegerKey(ASAtom.FF);
	}

	public Long getStructParent() {
		return getIntegerKey(ASAtom.STRUCT_PARENT);
	}
	
	public static boolean isField(COSObject obj) {
		return obj != null && !obj.empty() && obj.knownKey(ASAtom.T);
	}

}
