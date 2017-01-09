package org.verapdf.pd.images;

import org.verapdf.as.ASAtom;
import org.verapdf.cos.COSDictionary;
import org.verapdf.cos.COSObjType;
import org.verapdf.cos.COSObject;
import org.verapdf.cos.COSStream;
import org.verapdf.pd.PDContentStream;
import org.verapdf.pd.PDGroup;
import org.verapdf.pd.PDMetadata;
import org.verapdf.pd.PDResources;
import org.verapdf.tools.TypeConverter;

/**
 * @author Maksim Bezrukov
 */
public class PDXForm extends PDXObject implements PDContentStream {

	public PDXForm(COSObject obj) {
		super(obj);
	}

	@Override
	public ASAtom getType() {
		return ASAtom.FORM;
	}

	@Override
	public COSObject getContents() {
		return super.getObject();
	}

	@Override
	public void setContents(COSObject contents) {
		super.setObject(contents);
	}

	public ASAtom getSubtype2() {
		return getObject().getNameKey(ASAtom.SUBTYPE_2);
	}

	public PDGroup getGroup() {
		COSObject group = getKey(ASAtom.GROUP);
		if (group != null && group.getType() == COSObjType.COS_DICT) {
			return new PDGroup(group);
		}
		return null;
	}

	public COSStream getPS() {
		COSObject ps = getKey(ASAtom.PS);
		if (ps != null && ps.getType() == COSObjType.COS_STREAM) {
			return (COSStream) ps.getDirectBase();
		}
		return null;
	}

	public COSDictionary getRef() {
		COSObject ref = getKey(ASAtom.REF);
		if (ref != null && ref.getType() == COSObjType.COS_DICT) {
			return (COSDictionary) ref.getDirectBase();
		}
		return null;
	}

	public double[] getBBox() {
		return TypeConverter.getRealArray(getKey(ASAtom.BBOX), 4, "BBox");
	}

	public double[] getMatrix() {
		double[] matrix = TypeConverter.getRealArray(getKey(ASAtom.MATRIX), 6, "Matrix");
		if (matrix == null) {
			matrix = new double[]{1, 0, 0, 1, 0, 0};
		}
		return matrix;
	}

	public PDResources getResources() {
		COSObject res = getKey(ASAtom.RESOURCES);
		if (res != null && res.getType() == COSObjType.COS_DICT) {
			return new PDResources(res);
		}
		return new PDResources(COSDictionary.construct());
	}

	public PDMetadata getMetadata() {
		COSObject meta = getKey(ASAtom.METADATA);
		if (meta != null && meta.getType() == COSObjType.COS_STREAM) {
			return new PDMetadata(meta);
		}
		return null;
	}

	public Long getStructParents() {
		return getObject().getIntegerKey(ASAtom.STRUCT_PARENTS);
	}
}
