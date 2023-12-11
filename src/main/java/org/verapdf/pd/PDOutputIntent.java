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
package org.verapdf.pd;

import org.verapdf.as.ASAtom;
import org.verapdf.cos.COSKey;
import org.verapdf.cos.COSObjType;
import org.verapdf.cos.COSObject;
import org.verapdf.external.ICCProfile;

import java.util.logging.Level;
import java.util.logging.Logger;
/**
 * @author Maksim Bezrukov
 */
public class PDOutputIntent extends PDObject {
	private static final Logger LOGGER = Logger.getLogger(PDOutputIntent.class.getCanonicalName());

	public PDOutputIntent(COSObject obj) {
		super(obj);
		if (getOutputConditionIdentifier() == null) {
			LOGGER.log(Level.WARNING, "Missing OutputConditionIdentifier in an output intent dictionary");
		}
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

	public String getSubtype() {
		COSObject base = getKey(ASAtom.S);
		if (base != null && base.getType() == COSObjType.COS_NAME) {
			return base.getName().getValue();
		}
		return null;
	}

	public String getColorSpace() {
		ICCProfile iccProfile = getDestOutputProfile();
		if (iccProfile != null && "GTS_PDFA1".equals(getSubtype())) {
			return iccProfile.getColorSpace();
		}
		return null;
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

	public String getDestOutputProfileIndirect() {
		COSObject obj = getKey(ASAtom.DEST_OUTPUT_PROFILE);
		if (!obj.empty() && obj.isIndirect()) {
			COSKey key = obj.getKey();
			return key.getNumber() + " " + key.getGeneration();
		}
		return null;
	}
}
