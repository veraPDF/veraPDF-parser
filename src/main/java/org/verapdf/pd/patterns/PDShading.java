package org.verapdf.pd.patterns;

import org.apache.log4j.Logger;
import org.verapdf.as.ASAtom;
import org.verapdf.cos.COSObject;
import org.verapdf.factory.colors.ColorSpaceFactory;
import org.verapdf.pd.PDResource;
import org.verapdf.pd.colors.PDColorSpace;
import org.verapdf.tools.TypeConverter;

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
		return TypeConverter.getRealArray(getKey(ASAtom.BBOX), 4, "BBox");
	}

	public boolean getAntiAlias() {
		Boolean antiAlias = getObject().getBooleanKey(ASAtom.ANTI_ALIAS);
		return antiAlias == null ? false : antiAlias.booleanValue();
	}
}
