package org.verapdf.tools;

import org.verapdf.as.ASAtom;
import org.verapdf.cos.COSArray;
import org.verapdf.cos.COSObjType;
import org.verapdf.cos.COSObject;

public class AttributeHelper {

	public static final String PRINT_FIELD = "PrintField";
	public static final String LAYOUT = "Layout";

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
		COSObject aValue = simplePDObject.getKey(ASAtom.A);
		if (aValue == null) {
			return COSObject.getEmpty();
		}
		if (aValue.getType() == COSObjType.COS_ARRAY) {
			for (COSObject object : (COSArray) aValue.getDirectBase()) {
				COSObject value = getAttributeValue(object, attributeName, O);
				if (value.getType() == type) {
					return value;
				}
			}
		}
		COSObject value = getAttributeValue(aValue, attributeName, O);
		return value.getType() == type ? value : COSObject.getEmpty();
	}

	private static COSObject getAttributeValue(COSObject object, ASAtom attributeName, String O) {
		if (object.getType() == COSObjType.COS_DICT && O.equals(object.getNameKeyStringValue(ASAtom.O))) {
			return object.getKey(attributeName);
		}
		return COSObject.getEmpty();
	}
}
