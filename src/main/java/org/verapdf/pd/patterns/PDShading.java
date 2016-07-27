package org.verapdf.pd.patterns;

import org.apache.log4j.Logger;
import org.verapdf.as.ASAtom;
import org.verapdf.cos.COSObjType;
import org.verapdf.cos.COSObject;
import org.verapdf.factory.colors.ColorSpaceFactory;
import org.verapdf.pd.PDResource;
import org.verapdf.pd.colors.PDColorSpace;

/**
 * @author Maksim Bezrukov
 */
public class PDShading extends PDResource {

	private static final Logger LOGGER = Logger.getLogger(PDShading.class);

	public PDShading(COSObject obj) {
		super(obj);
	}

	public int getShadingType() {
		Long type = getObject().getIntegerKey(ASAtom.SHADING_TYPE);
		if (type != null) {
			return type.intValue();
		} else {
			LOGGER.debug("Shading object do not contain required key ShadingType");
			return 0;
		}
	}

	public PDColorSpace getColorSpace() {
		COSObject obj = getObject().getKey(ASAtom.COLORSPACE);
		if (obj != null && !obj.empty()) {
			return ColorSpaceFactory.getColorSpace(obj);
		} else {
			LOGGER.debug("Shading object do not contain required key ColorSpace");
			return null;
		}
	}

	public double[] getBBox() {
		COSObject bboxObject = getObject().getKey(ASAtom.BBOX);
		if (bboxObject != null && bboxObject.getType() == COSObjType.COS_ARRAY) {
			int size = bboxObject.size();
			if (size != 4) {
				LOGGER.debug("BBox array doesn't consist of 4 elements");
			}
			double[] res = new double[size];
			for (int i = 0; i < size; ++i) {
				COSObject number = bboxObject.at(i);
				if (number == null || number.getReal() == null) {
					LOGGER.debug("BBox array contains non number value");
					return null;
				} else {
					res[i] = number.getReal();
				}
			}
			return res;
		}
		return null;
	}

	public boolean getAntiAlias() {
		Boolean antiAlias = getObject().getBooleanKey(ASAtom.ANTI_ALIAS);
		return antiAlias == null ? false : antiAlias.booleanValue();
	}
}
