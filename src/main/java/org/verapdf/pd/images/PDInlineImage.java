package org.verapdf.pd.images;

import org.verapdf.as.ASAtom;
import org.verapdf.cos.COSArray;
import org.verapdf.cos.COSName;
import org.verapdf.cos.COSObjType;
import org.verapdf.cos.COSObject;
import org.verapdf.factory.colors.ColorSpaceFactory;
import org.verapdf.pd.PDResource;
import org.verapdf.pd.PDResources;
import org.verapdf.pd.colors.PDColorSpace;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Maksim Bezrukov
 */
public class PDInlineImage extends PDResource {

	private static final Logger LOGGER = Logger.getLogger(PDInlineImage.class.getCanonicalName());

	private PDResources imageResources;
	private PDResources pageResources;

	public PDInlineImage(COSObject obj, PDResources imageResources,
						 PDResources pageResources) {
		super(obj);
		this.imageResources = imageResources;
		this.pageResources = pageResources;
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
						LOGGER.log(Level.FINE, "Filter array contains non name value");
						return Collections.emptyList();
					}
					res.add((COSName) filter.getDirectBase());
				}
			}
			return Collections.unmodifiableList(res);
		}
		return Collections.emptyList();
	}

	public PDColorSpace getImageCS() {
		COSObject cs = getKey(ASAtom.CS);
		if (cs.empty()) {
			cs = getKey(ASAtom.COLORSPACE);
		}
		if (cs != null && cs.getType() == COSObjType.COS_NAME) {
			replaceAbbreviation((COSName) cs.getDirectBase());
			PDColorSpace result = getDefaultColorSpace(cs.getName());
			if (result != null) {
				return result;
			}
		}
		PDColorSpace result = ColorSpaceFactory.getColorSpace(cs, imageResources);
		if (result == null) {
			result = ColorSpaceFactory.getColorSpace(cs, pageResources);
		}
		return result;
	}

	private PDColorSpace getDefaultColorSpace(ASAtom name) {
		if (isDeviceDependent(name)) {
			if (imageResources != null) {
				ASAtom value = org.verapdf.factory.colors.ColorSpaceFactory.getDefaultValue(imageResources, name);
				if (value != null) {
					return imageResources.getColorSpace(value);
				}
			} else {
				ASAtom value = org.verapdf.factory.colors.ColorSpaceFactory.getDefaultValue(pageResources, name);
				if (value != null) {
					return pageResources.getColorSpace(value);
				}
			}
		}
		return null;
	}

	private static boolean isDeviceDependent(ASAtom name) {
		return ASAtom.DEVICERGB.equals(name) ||
				ASAtom.DEVICEGRAY.equals(name) || ASAtom.DEVICECMYK.equals(name);
	}

	private static void replaceAbbreviation(final COSName abbreviation) {
		if (abbreviation.getName() == ASAtom.CMYK) {
			abbreviation.set(ASAtom.DEVICECMYK);
		} else if (abbreviation.getName() == ASAtom.RGB) {
			abbreviation.set(ASAtom.DEVICERGB);
		} else if (abbreviation.getName() == ASAtom.G) {
			abbreviation.set(ASAtom.DEVICEGRAY);
		}
	}

	public COSName getIntent() {
		COSObject object = getKey(ASAtom.INTENT);
		if (object != null && object.getType() == COSObjType.COS_NAME) {
			return (COSName) object.getDirectBase();
		}
		return null;
	}

}
