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

	public static String getListNumbering(org.verapdf.pd.PDObject simplePDObject) {
		return AttributeHelper.getNameAttributeValue(simplePDObject, ASAtom.LIST_NUMBERING, LIST, NONE);
	}

	public static String getNoteType(org.verapdf.pd.PDObject simplePDObject) {
		return AttributeHelper.getNameAttributeValue(simplePDObject, ASAtom.NOTE_TYPE, TaggedPDFConstants.FENOTE, NONE);
	}
	public static Long getColSpan(org.verapdf.pd.PDObject simplePDObject) {
		return AttributeHelper.getIntegerAttributeValue(simplePDObject, ASAtom.COL_SPAN, TaggedPDFConstants.TABLE, 1L);
	}

	public static Long getRowSpan(org.verapdf.pd.PDObject simplePDObject) {
		return AttributeHelper.getIntegerAttributeValue(simplePDObject, ASAtom.ROW_SPAN, TaggedPDFConstants.TABLE, 1L);
	}

	public static String getScope(org.verapdf.pd.PDObject simplePDObject) {
		return AttributeHelper.getNameAttributeValue(simplePDObject, ASAtom.SCOPE, TaggedPDFConstants.TABLE, null);
	}

	public static String getRole(org.verapdf.pd.PDObject simplePDObject) {
		return AttributeHelper.getNameAttributeValue(simplePDObject, ASAtom.ROLE, PRINT_FIELD, null);
	}

	public static COSArray getBBox(org.verapdf.pd.PDObject simplePDObject) {
		return AttributeHelper.getArrayAttributeValue(simplePDObject, ASAtom.BBOX, LAYOUT, null);
	}

	public static COSArray getArrayAttributeValue(org.verapdf.pd.PDObject simplePDObject, ASAtom attributeName, String O,
	                                              COSArray defaultValue) {
		COSObject object = getAttributeValue(simplePDObject, attributeName, O, COSObjType.COS_ARRAY);
		if (object.getType() == COSObjType.COS_ARRAY) {
			return (COSArray) object.getDirectBase();
		}
		return defaultValue;
	}

	private static String getNameAttributeValue(org.verapdf.pd.PDObject simplePDObject, ASAtom attributeName, String O,
	                                            String defaultValue) {
		COSObject object = getAttributeValue(simplePDObject, attributeName, O, COSObjType.COS_NAME);
		if (object.getType() == COSObjType.COS_NAME) {
			return object.getString();
		}
		return defaultValue;
	}

	private static Long getIntegerAttributeValue(org.verapdf.pd.PDObject simplePDObject, ASAtom attributeName, String O,
	                                             Long defaultValue) {
		COSObject object = getAttributeValue(simplePDObject, attributeName, O, COSObjType.COS_INTEGER);
		if (object.getType() == COSObjType.COS_INTEGER) {
			return object.getInteger();
		}
		return defaultValue;
	}

	private static COSObject getAttributeValue(org.verapdf.pd.PDObject simplePDObject, ASAtom attributeName, String O,
											   COSObjType type) {
		COSObject attributeValue = getAttributeValue(simplePDObject.getKey(ASAtom.A), attributeName, O, type);
		if (attributeValue == null) {
			COSObject className = simplePDObject.getKey(ASAtom.C);
			COSObject classMap = StaticResources.getDocument().getStructTreeRoot().getClassMap();
			if (className != null && classMap != null) {
				if (className.getType() == COSObjType.COS_NAME) {
					attributeValue = getAttributeValue(classMap.getKey(className.getName()), 
							attributeName, O, type);
				} else if (className.getType() == COSObjType.COS_ARRAY) {
					for (COSObject entry : (COSArray)className.getDirectBase()) {
						if (entry != null && entry.getType() == COSObjType.COS_NAME) {
							attributeValue = getAttributeValue(classMap.getKey(entry.getName()),
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
	
	private static COSObject getAttributeValue(COSObject attributeObject, ASAtom attributeName, String O,
	                                           COSObjType type) {
		if (attributeObject == null) {
			return null;
		}
		if (attributeObject.getType() == COSObjType.COS_ARRAY) {
			for (COSObject object : (COSArray) attributeObject.getDirectBase()) {
				COSObject value = getAttributeValue(object, attributeName, O);
				if (value.getType() == type) {
					return value;
				}
			}
		}
		COSObject value = getAttributeValue(attributeObject, attributeName, O);
		return value.getType() == type ? value : null;
	}

	private static COSObject getAttributeValue(COSObject object, ASAtom attributeName, String O) {
		if (object.getType() == COSObjType.COS_DICT && O.equals(object.getNameKeyStringValue(ASAtom.O))) {
			return object.getKey(attributeName);
		}
		return COSObject.getEmpty();
	}
}
