package org.verapdf.pd.structure;

import org.verapdf.as.ASAtom;
import org.verapdf.cos.COSObjType;
import org.verapdf.cos.COSObject;
import org.verapdf.tools.TaggedPDFHelper;

/**
 * @author Maksim Bezrukov
 */
public class StructureType {
	private ASAtom type;
	private PDStructureNameSpace nameSpace;

	private StructureType(ASAtom type, PDStructureNameSpace nameSpace) {
		this.type = type;
		this.nameSpace = nameSpace;
	}

	public static StructureType createStructureType(COSObject object) {
		if (object == null) {
			throw new IllegalArgumentException("Argument object can not be null");
		}
		COSObjType objType = object.getType();
		if (objType == COSObjType.COS_NAME) {
			return createStructureType(object, null);
		} else if (objType == COSObjType.COS_ARRAY && object.size() >= 2) {
			return createStructureType(object.at(0), object.at(1));
		}
		return null;
	}

	public static StructureType createStructureType(COSObject type, COSObject ns) {
		if (type != null && type.getType() == COSObjType.COS_NAME) {
			if (ns != null && ns.getType() == COSObjType.COS_DICT) {
				return new StructureType(type.getName(), PDStructureNameSpace.createNameSpace(ns));
			} else {
				return new StructureType(type.getName(), null);
			}
		}
		return null;
	}

	public static StructureType createStructureType(ASAtom type) {
		if (type != null) {
			return new StructureType(type, null);
		}
		return null;
	}

	public ASAtom getType() {
		return type;
	}

	public String getNameSpaceURI() {
		return this.nameSpace == null ? TaggedPDFHelper.PDF_NAMESPACE : this.nameSpace.getNS();
	}

	public PDStructureNameSpace getNameSpace() {
		return nameSpace;
	}
}
