/**
 * This file is part of veraPDF Parser, a module of the veraPDF project.
 * Copyright (c) 2015-2024, veraPDF Consortium <info@verapdf.org>
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
package org.verapdf.pd.colors;

import org.verapdf.as.ASAtom;
import org.verapdf.as.io.ASInputStream;
import org.verapdf.as.io.ASMemoryInStream;
import org.verapdf.cos.COSKey;
import org.verapdf.cos.COSObjType;
import org.verapdf.cos.COSObject;
import org.verapdf.cos.COSStream;
import org.verapdf.external.ICCProfile;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Maksim Bezrukov
 */
public class PDICCBased extends PDColorSpace {

	private static final Logger LOGGER = Logger.getLogger(PDICCBased.class.getCanonicalName());
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
		COSObject stream = obj.at(1);
		if (stream != null && stream.getType() == COSObjType.COS_STREAM) {
			this.iccProfile = new ICCProfile(stream);
			Long n = this.iccProfile.getNumberOfColorants();
			this.numberOfComponents = n == null ? -1 : n.intValue();
		} else {
			this.iccProfile = null;
			this.numberOfComponents = -1;
		}
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

	@Override
	public double[] toRGB(double[] value) {
		return getAlternate().toRGB(value);
	}

	public String getColorSpaceType() {
		String colorSpaceType = null;
		if (iccProfile != null) {
			colorSpaceType = iccProfile.getColorSpace();
		}
		return colorSpaceType;
	}

	public String getICCProfileIndirect() {
		if (iccProfile != null) {
			COSKey key = this.iccProfile.getObject().getKey();
			if (key != null) {
				return key.getNumber() + " " + key.getGeneration();
			}
		}
		return null;
	}

	public String getICCProfileMD5() {
		return iccProfile != null ? this.iccProfile.getMD5() : null;
	}

	public double[] getRange() {
		return this.iccProfile == null ? null : this.iccProfile.getRange();
	}

	public PDColorSpace getAlternate() {
		if (this.iccProfile == null) {
			return null;
		}
		PDColorSpace res = this.iccProfile.getAlternate();
		if (res == null) {
			switch (this.numberOfComponents) {
				case 1:
					res = PDDeviceGray.INSTANCE;
					break;
				case 3:
					res = PDDeviceRGB.INSTANCE;
					break;
				case 4:
					res = PDDeviceCMYK.INSTANCE;
					break;
				default:
					LOGGER.log(Level.FINE, "Unknown amount of components in icc based colorspace (" +
							this.numberOfComponents + ')');
			}
		}
		return res;
	}
}
