package org.verapdf.pd;

import org.verapdf.as.ASAtom;
import org.verapdf.cos.COSObjType;
import org.verapdf.cos.COSObject;
import org.verapdf.pd.actions.PDAction;
import org.verapdf.pd.actions.PDAnnotationAdditionalActions;
import org.verapdf.tools.TypeConverter;

/**
 * @author Maksim Bezrukov
 */
public class PDAnnotation extends PDObject {

	public PDAnnotation(COSObject obj) {
		super(obj);
	}

	public ASAtom getSubtype() {
		return getObject().getNameKey(ASAtom.SUBTYPE);
	}

	public Long getF() {
		return getObject().getIntegerKey(ASAtom.F);
	}

	public Double getCA() {
		return getObject().getRealKey(ASAtom.CA);
	}

	public ASAtom getFT() {
		return getObject().getNameKey(ASAtom.FT);
	}

	public double[] getRect() {
		return TypeConverter.getRealArray(getKey(ASAtom.RECT), 4, "Rect");
	}

	public COSObject getCOSC(){
		COSObject res = getKey(ASAtom.C);
		if (res != null && res.getType() == COSObjType.COS_ARRAY) {
			return res;
		}
		return null;
	}

	public COSObject getCOSIC(){
		COSObject res = getKey(ASAtom.IC);
		if (res != null && res.getType() == COSObjType.COS_ARRAY) {
			return res;
		}
		return null;
	}

	public COSObject getCOSAP() {
		COSObject appearanceDictionary = getKey(ASAtom.AP);
		if (appearanceDictionary != null && appearanceDictionary.getType() == COSObjType.COS_DICT) {
			return appearanceDictionary;
		}
		return null;
	}

	public PDAppearanceEntry getNormalAppearance() {
		return getAppearanceEntry(ASAtom.N);
	}

	public PDAppearanceEntry getRolloverAppearance() {
		return getAppearanceEntry(ASAtom.R);
	}

	public PDAppearanceEntry getDownAppearance() {
		return getAppearanceEntry(ASAtom.D);
	}

	private PDAppearanceEntry getAppearanceEntry(ASAtom key) {
		COSObject appearanceDictionary = getCOSAP();
		if (appearanceDictionary != null) {
			COSObject appearance = appearanceDictionary.getKey(key);
			if (appearance != null && appearance.getType().isDictionaryBased()) {
				return new PDAppearanceEntry(appearance);
			}
		}
		return null;
	}

	public PDAction getA() {
		COSObject action = getKey(ASAtom.A);
		if (action != null && action.getType() == COSObjType.COS_DICT) {
			return new PDAction(action);
		}
		return null;
	}

	public PDAnnotationAdditionalActions getAdditionalActions() {
		COSObject aa = getKey(ASAtom.AA);
		if (aa != null && aa.getType() == COSObjType.COS_DICT) {
			return new PDAnnotationAdditionalActions(aa);
		}
		return null;
	}
}
