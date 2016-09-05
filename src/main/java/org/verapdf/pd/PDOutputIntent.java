package org.verapdf.pd;

import org.verapdf.as.ASAtom;
import org.verapdf.cos.COSObjType;
import org.verapdf.cos.COSObject;
import org.verapdf.external.ICCProfile;

/**
 * @author Maksim Bezrukov
 */
public class PDOutputIntent extends PDObject {

	public PDOutputIntent(COSObject obj) {
		super(obj);
	}

	public String getOutputCondition() {
		return getStringValue(ASAtom.OUTPUT_CONDITION);
	}

	public String getOutputConditionIdentifier() {
		return getStringValue(ASAtom.OUTPUT_CONDITION_IDENTIFIER);
	}

	public String getRegistryName() {
		return getStringValue(ASAtom.REGISTRY_NAME);
	}

	public String getInfo() {
		return getStringValue(ASAtom.INFO);
	}

	public ICCProfile getDestOutputProfile() {
		COSObject profile = getKey(ASAtom.DEST_OUTPUT_PROFILE);
		if (profile != null && profile.getType() == COSObjType.COS_STREAM) {
			return new ICCProfile(profile);
		}
		return null;
	}

	public COSObject getCOSDestOutputProfileRef() {
		return getKey(ASAtom.DEST_OUTPUT_PROFILE_REF);
	}

	private String getStringValue(ASAtom key) {
		COSObject base = getKey(key);
		if (base != null && base.getType() == COSObjType.COS_STRING) {
			return base.getString();
		}
		return null;
	}
}
