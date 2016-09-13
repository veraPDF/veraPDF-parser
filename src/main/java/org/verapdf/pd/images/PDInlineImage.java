package org.verapdf.pd.images;

import org.apache.log4j.Logger;
import org.verapdf.as.ASAtom;
import org.verapdf.cos.COSArray;
import org.verapdf.cos.COSName;
import org.verapdf.cos.COSObjType;
import org.verapdf.cos.COSObject;
import org.verapdf.factory.colors.ColorSpaceFactory;
import org.verapdf.pd.PDResource;
import org.verapdf.pd.colors.PDColorSpace;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Maksim Bezrukov
 */
public class PDInlineImage extends PDResource {

	private static final Logger LOGGER = Logger.getLogger(PDInlineImage.class);

	public PDInlineImage(COSObject obj) {
		super(obj);
	}

	public boolean isInterpolate() {
		Boolean value = getObject().getBooleanKey(ASAtom.INTERPOLATE);
		value = value == null ? getObject().getBooleanKey(ASAtom.I) : value;
		return value != null ? value.booleanValue() : false;
	}

	public List<COSName> getCOSFilters() {
		COSObject filters = getKey(ASAtom.FILTER);
		if (filters == null || filters.empty()) {
			filters = getKey(ASAtom.F);
		}

		if (filters != null) {
			List<COSName> res = new ArrayList<>();
			if (filters.getType() == COSObjType.COS_NAME) {
				res.add((COSName) filters.getDirectBase());
			} else if (filters.getType() == COSObjType.COS_ARRAY) {
				for (COSObject filter : ((COSArray) filters.getDirectBase())) {
					if (filter == null || filter.getType() != COSObjType.COS_NAME) {
						LOGGER.debug("Filter array contains non name value");
						return Collections.emptyList();
					} else {
						res.add((COSName) filter.getDirectBase());
					}
				}
			}
			return Collections.unmodifiableList(res);
		}
		return Collections.emptyList();
	}

	public PDColorSpace getImageCS() {
		return ColorSpaceFactory.getColorSpace(getKey(ASAtom.COLORSPACE));
	}

	public COSName getIntent() {
		COSObject object = getKey(ASAtom.INTENT);
		if (object != null && object.getType() == COSObjType.COS_NAME) {
			return (COSName) object.getDirectBase();
		}
		return null;
	}
}
