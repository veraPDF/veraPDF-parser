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
import org.verapdf.exceptions.LoopedException;
import org.verapdf.pd.actions.PDAction;
import org.verapdf.pd.actions.PDAnnotationAdditionalActions;
import org.verapdf.tools.TypeConverter;

import java.util.HashSet;
import java.util.Set;

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

	public String getContents() {
		return getStringKey(ASAtom.CONTENTS);
	}

	public String getAnnotationName() {
		return getStringKey(ASAtom.NM);
	}

	public String getModDate() {
		return getStringKey(ASAtom.M);
	}

	public Double getCA() {
		return getObject().getRealKey(ASAtom.CA);
	}

	public ASAtom getFT() {
		Set<COSKey> visitedKeys = new HashSet<>();
		COSObject curr = getObject();
		while (curr != null && curr.getType().isDictionaryBased()) {
			COSKey key = curr.getObjectKey();
			if (key != null) {
				if (visitedKeys.contains(key)) {
					throw new LoopedException("Loop in field tree");
				}
				visitedKeys.add(key);
			}
			if (curr.knownKey(ASAtom.FT)) {
				return curr.getNameKey(ASAtom.FT);
			}
			curr = curr.getKey(ASAtom.PARENT);
		}
		return null;
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

	public PDAnnotation getPopup() {
		COSObject popup = getKey(ASAtom.POPUP);
		if (popup != null && popup.getType().isDictionaryBased()) {
			return new PDAnnotation(popup);
		}
		return null;
	}

	public double[] getColor() {
		return TypeConverter.getRealArray(getKey(ASAtom.C), "Color");
	}

	public boolean isInvisible() {
		return getFlagValue(0);
	}

	public boolean isHidden() {
		return getFlagValue(1);
	}

	public boolean isPrinted() {
		return getFlagValue(2);
	}

	public boolean isNoZoom() {
		return getFlagValue(3);
	}

	public boolean isNoRotate() {
		return getFlagValue(4);
	}

	public boolean isNoView() {
		return getFlagValue(5);
	}

	public boolean isReadOnly() {
		return getFlagValue(6);
	}

	public boolean isLocked() {
		return getFlagValue(7);
	}

	public boolean isToggleNoView() {
		return getFlagValue(8);
	}

	public boolean isLockedContents() {
		return getFlagValue(9);
	}

	private boolean getFlagValue(int index) {
		Long flag = getIntegerKey(ASAtom.F);
		if (flag != null) {
			long f = flag.longValue();
			int bitFlag = 1 << index;
			return (f & bitFlag) == bitFlag;
		}
		return false;
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
