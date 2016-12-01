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
		return null;
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
