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
package org.verapdf.pd.images;

import org.verapdf.as.ASAtom;
import org.verapdf.cos.*;
import org.verapdf.factory.colors.ColorSpaceFactory;
import org.verapdf.pd.PDResource;
import org.verapdf.pd.PDResources;
import org.verapdf.pd.colors.PDColorSpace;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Maksim Bezrukov
 */
public class PDInlineImage extends PDResource {

	private static Map<ASAtom, ASAtom> abbreviationsMap = new HashMap<>();
	private static Map<ASAtom, ASAtom> abbreviationsFiltersAndColorSpaceMap = new HashMap<>();

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
		COSObject interpolate = getInlineImageKey(getObject().get(), ASAtom.INTERPOLATE);
		Boolean result = interpolate.getBoolean();
		return result != null ? result.booleanValue() : false;
	}

	public Long getBitsPerComponent() {
		COSObject bitsPerComponent = getInlineImageKey(getObject().get(), ASAtom.BITS_PER_COMPONENT);
		return bitsPerComponent.getInteger();
	}

	public List<COSName> getCOSFilters() {
		COSObject filters = getInlineImageKey(getObject().get(), ASAtom.FILTER);

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
		COSObject cs = getInlineImageKey(getObject().get(), ASAtom.COLORSPACE);
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

	public boolean getImageMask() {
		COSObject im = getInlineImageKey(getObject().get(), ASAtom.IMAGE_MASK);
		Boolean result = im.getBoolean();
		return result != null ? result.booleanValue() : false;
	}

	public static COSObject getInlineImageKey(COSBase inlineImage, ASAtom key) {
		if (inlineImage == null) {
			return COSObject.getEmpty();
		}
		ASAtom abbreviation = abbreviationsMap.get(key);
		return abbreviation != null && inlineImage.knownKey(abbreviation) ?
				inlineImage.getKey(abbreviation) : inlineImage.getKey(key);
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

	public static void replaceAbbreviation(final COSName abbreviation) {
		ASAtom name = abbreviationsFiltersAndColorSpaceMap.get(abbreviation.getName());
		if (name != null) {
			abbreviation.set(name);
		}
	}

	public COSName getIntent() {
		COSObject object = getKey(ASAtom.INTENT);
		if (object != null && object.getType() == COSObjType.COS_NAME) {
			return (COSName) object.getDirectBase();
		}
		return null;
	}

	static {
		abbreviationsMap.put(ASAtom.BITS_PER_COMPONENT, ASAtom.BPC);
		abbreviationsMap.put(ASAtom.COLORSPACE, ASAtom.CS);
		abbreviationsMap.put(ASAtom.DECODE, ASAtom.D);
		abbreviationsMap.put(ASAtom.DECODE_PARMS, ASAtom.DP);
		abbreviationsMap.put(ASAtom.FILTER, ASAtom.F);
		abbreviationsMap.put(ASAtom.HEIGHT, ASAtom.H);
		abbreviationsMap.put(ASAtom.IMAGE_MASK, ASAtom.IM);
		abbreviationsMap.put(ASAtom.INTERPOLATE, ASAtom.I);
		abbreviationsMap.put(ASAtom.LENGTH, ASAtom.L);
		abbreviationsMap.put(ASAtom.WIDTH, ASAtom.W);

		abbreviationsFiltersAndColorSpaceMap.put(ASAtom.G, ASAtom.DEVICEGRAY);
		abbreviationsFiltersAndColorSpaceMap.put(ASAtom.RGB, ASAtom.DEVICERGB);
		abbreviationsFiltersAndColorSpaceMap.put(ASAtom.CMYK, ASAtom.DEVICECMYK);
		abbreviationsFiltersAndColorSpaceMap.put(ASAtom.I, ASAtom.INDEXED);
		abbreviationsFiltersAndColorSpaceMap.put(ASAtom.ASCII_HEX_DECODE_ABBREVIATION, ASAtom.ASCII_HEX_DECODE);
		abbreviationsFiltersAndColorSpaceMap.put(ASAtom.ASCII85_DECODE_ABBREVIATION, ASAtom.ASCII85_DECODE);
		abbreviationsFiltersAndColorSpaceMap.put(ASAtom.LZW_DECODE_ABBREVIATION, ASAtom.LZW_DECODE);
		abbreviationsFiltersAndColorSpaceMap.put(ASAtom.FLATE_DECODE_ABBREVIATION, ASAtom.FLATE_DECODE);
		abbreviationsFiltersAndColorSpaceMap.put(ASAtom.RUN_LENGTH_DECODE_ABBREVIATION, ASAtom.RUN_LENGTH_DECODE);
		abbreviationsFiltersAndColorSpaceMap.put(ASAtom.CCITTFAX_DECODE_ABBREVIATION, ASAtom.CCITTFAX_DECODE);
		abbreviationsFiltersAndColorSpaceMap.put(ASAtom.DCT_DECODE_ABBREVIATION, ASAtom.DCT_DECODE);
	}

}
