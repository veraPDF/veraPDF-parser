/**
 * This file is part of veraPDF Parser, a module of the veraPDF project.
 * Copyright (c) 2015-2025, veraPDF Consortium <info@verapdf.org>
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

import org.verapdf.as.ASAtom;
import org.verapdf.cos.COSArray;
import org.verapdf.cos.COSObjType;
import org.verapdf.cos.COSObject;

public class AttributeHelper {

	public static final String PRINT_FIELD = "PrintField";
	public static final String LAYOUT = "Layout";
	public static final String LIST = "List";
	public static final String NONE = "None";

	public static String getListNumbering(COSObject simpleCosObject) {
		return getNameAttributeValue(simpleCosObject, ASAtom.LIST_NUMBERING, LIST, NONE, true);
	}

	public static String getNoteType(COSObject simpleCosObject) {
		return getNameAttributeValue(simpleCosObject, ASAtom.NOTE_TYPE, TaggedPDFConstants.FENOTE, NONE, false);
	}
	public static Long getColSpan(COSObject simpleCosObject) {
		return getIntegerAttributeValue(simpleCosObject, ASAtom.COL_SPAN, TaggedPDFConstants.TABLE, 1L);
	}

	public static Long getRowSpan(COSObject simpleCosObject) {
		return getIntegerAttributeValue(simpleCosObject, ASAtom.ROW_SPAN, TaggedPDFConstants.TABLE, 1L);
	}

	public static String getScope(COSObject simpleCosObject) {
		return getNameAttributeValue(simpleCosObject, ASAtom.SCOPE, TaggedPDFConstants.TABLE, null, false);
	}

	public static String getRole(COSObject simpleCosObject) {
		return getNameAttributeValue(simpleCosObject, ASAtom.ROLE, PRINT_FIELD, null, false);
	}

	public static COSArray getBBox(COSObject simpleCosObject) {
		return getArrayAttributeValue(simpleCosObject, ASAtom.BBOX, LAYOUT, null);
	}

	public static COSArray getArrayAttributeValue(COSObject simpleCosObject, ASAtom attributeName, String O,
	                                              COSArray defaultValue) {
		COSObject object = getAttributeValue(simpleCosObject, attributeName, O, COSObjType.COS_ARRAY);
		if (object.getType() == COSObjType.COS_ARRAY) {
			return (COSArray) object.getDirectBase();
		}
		return defaultValue;
	}

	private static String getNameAttributeValue(COSObject simpleCosObject, ASAtom attributeName, String O,
	                                            String defaultValue, boolean isInheritable) {
		COSObject object = getAttributeValue(simpleCosObject, attributeName, O, COSObjType.COS_NAME);
		if (object.getType() == COSObjType.COS_NAME) {
			return object.getString();
		}
		if (isInheritable) {
			COSObject parent = simpleCosObject.getKey(ASAtom.P);
			if (parent != null) {
				return getNameAttributeValue(parent, attributeName, O, defaultValue, true);
			}
		}
		return defaultValue;
	}

	private static Long getIntegerAttributeValue(COSObject simpleCosObject, ASAtom attributeName, String O,
	                                             Long defaultValue) {
		COSObject object = getAttributeValue(simpleCosObject, attributeName, O, COSObjType.COS_INTEGER);
		if (object.getType() == COSObjType.COS_INTEGER) {
			return object.getInteger();
		}
		return defaultValue;
	}

	private static COSObject getAttributeValue(COSObject simpleCosObject, ASAtom attributeName, String O,
											   COSObjType type) {
		COSObject attributeValue = getAttributeObject(simpleCosObject.getKey(ASAtom.A), attributeName, O, type);
		if (attributeValue == null) {
			COSObject className = simpleCosObject.getKey(ASAtom.C);
			COSObject classMap = StaticResources.getDocument().getStructTreeRoot().getClassMap();
			if (className != null && classMap != null) {
				if (className.getType() == COSObjType.COS_NAME) {
					attributeValue = getAttributeObject(classMap.getKey(className.getName()), 
							attributeName, O, type);
				} else if (className.getType() == COSObjType.COS_ARRAY) {
					for (COSObject entry : (COSArray)className.getDirectBase()) {
						if (entry != null && entry.getType() == COSObjType.COS_NAME) {
							attributeValue = getAttributeObject(classMap.getKey(entry.getName()),
									attributeName, O, type);
							if (attributeValue != null) {
								break;
							}
						}
					}
				}
			}
		}
		return attributeValue != null ? attributeValue : COSObject.getEmpty();
	}
	
	private static COSObject getAttributeObject(COSObject attributeObject, ASAtom attributeName, String O,
	                                           COSObjType type) {
		if (attributeObject == null) {
			return null;
		}
		if (attributeObject.getType() == COSObjType.COS_ARRAY) {
			for (COSObject object : (COSArray) attributeObject.getDirectBase()) {
				COSObject value = getAttributeObject(object, attributeName, O);
				if (value.getType() == type) {
					return value;
				}
			}
		}
		COSObject value = getAttributeObject(attributeObject, attributeName, O);
		return value.getType() == type ? value : null;
	}

	private static COSObject getAttributeObject(COSObject object, ASAtom attributeName, String O) {
		if (object.getType() == COSObjType.COS_DICT && O.equals(object.getNameKeyStringValue(ASAtom.O))) {
			return object.getKey(attributeName);
		}
		return COSObject.getEmpty();
	}
}
