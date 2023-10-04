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
import org.verapdf.cos.COSObjType;
import org.verapdf.cos.COSObject;
import org.verapdf.pd.actions.PDAction;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Maksim Bezrukov
 */
public class PDOutlineItem extends PDOutlineDictionary {

	private static final Logger LOGGER = Logger.getLogger(PDOutlineItem.class.getCanonicalName());

	public PDOutlineItem(COSObject obj) {
		super(obj);
	}

	public String getTitle() {
		return getStringKey(ASAtom.TITLE);
	}

	public PDOutlineItem getPrev() {
		return getOutlineItem(ASAtom.PREV);
	}

	public PDOutlineItem getNext() {
		return getOutlineItem(ASAtom.NEXT);
	}

	public PDAction getAction() {
		COSObject action = getKey(ASAtom.A);
		if (action != null && action.getType().isDictionaryBased()) {
			return new PDAction(action);
		}
		return null;
	}

	public COSObject getDestination() {
		return getKey(ASAtom.DEST);
	}

	public double[] getColor() {
		COSObject arr = getKey(ASAtom.C);
		if (arr != null && arr.getType() == COSObjType.COS_ARRAY) {
			if (arr.size().intValue() == 3) {
				Double redValue = arr.at(0).getReal();
				Double greenValue = arr.at(1).getReal();
				Double blueValue = arr.at(2).getReal();
				if (redValue == null || greenValue == null || blueValue == null) {
					LOGGER.log(Level.FINE, "Outline's color contains non number value");
					return null;
				}
				float red = redValue.floatValue();
				float green = greenValue.floatValue();
				float blue = blueValue.floatValue();
				if (red < 0 || red > 1 || green < 0 || green > 1 || blue < 0 || blue > 1) {
					LOGGER.log(Level.FINE, "Outline's color contains wrong value");
					return null;
				}
				return new double[]{red, green, blue};
			}
			LOGGER.log(Level.FINE, "Outline's color contains not three elements");
			return null;
		}
		return new double[]{0.0, 0.0, 0.0};
	}

	public boolean isItalic() {
		return isFlagBitSet(0);
	}

	public boolean isBold() {
		return isFlagBitSet(1);
	}

	private boolean isFlagBitSet(int bitNumber) {
		Long f = getIntegerKey(ASAtom.F);
		return f != null && (f.intValue() & (1 << bitNumber)) != 0;
	}
}
