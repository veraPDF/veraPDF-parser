package org.verapdf.pd.colors;

import org.verapdf.as.ASAtom;
import org.verapdf.as.io.ASInputStream;
import org.verapdf.cos.COSObject;
import org.verapdf.cos.COSStream;
import org.verapdf.external.ICCProfile;
import org.verapdf.io.ASMemoryInStream;

/**
 * @author Maksim Bezrukov
 */
public class PDICCBased extends PDColorSpace {

	private final ICCProfile iccProfile;
	private final int numberOfComponents;

	public PDICCBased(int numberOfComponents) {
		this.iccProfile = null;
		this.numberOfComponents = numberOfComponents;
	}

	public PDICCBased(int numberOfComponents, byte[] profile) {
		ASInputStream iccProfileStream = new ASMemoryInStream(profile);
		COSObject cosObject = COSStream.construct(iccProfileStream);
		cosObject.setIntegerKey(ASAtom.N, numberOfComponents);
		setObject(cosObject);
		this.iccProfile = new ICCProfile(cosObject);
		Long n = this.iccProfile.getNumberOfColorants();
		this.numberOfComponents = n == null ? -1 : n.intValue();
	}

	public PDICCBased(COSObject obj) {
		super(obj);
		this.iccProfile = new ICCProfile(obj);
		Long n = this.iccProfile.getNumberOfColorants();
		this.numberOfComponents = n == null ? -1 : n.intValue();
	}

	public ICCProfile getICCProfile() {
		return this.iccProfile;
	}

	@Override
	public int getNumberOfComponents() {
		return this.numberOfComponents;
	}

	@Override
	public ASAtom getType() {
		return ASAtom.ICCBASED;
	}

	public double[] getRange() {
		return this.iccProfile == null ? null : this.iccProfile.getRange();
	}

	public PDColorSpace getAlternate() {
		return this.iccProfile == null ? null : this.iccProfile.getAlternate();
	}

//	TODO: implement me
//	public PDMetadata getMetadata() {
//		return this.iccProfile == null ? null : this.iccProfile.getMetadata();
//	}
}
