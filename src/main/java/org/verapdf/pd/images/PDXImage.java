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
import org.verapdf.cos.COSArray;
import org.verapdf.cos.COSName;
import org.verapdf.cos.COSObjType;
import org.verapdf.cos.COSObject;
import org.verapdf.external.JPEG2000;
import org.verapdf.factory.colors.ColorSpaceFactory;
import org.verapdf.pd.PDMetadata;
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
public class PDXImage extends PDXObject {

	private static final Logger LOGGER = Logger.getLogger(PDXImage.class.getCanonicalName());

	private ASAtom colorSpaceName;
	private PDColorSpace imageCS;
	private PDResources resources;

	private JPEG2000 jpxStream;

	public PDXImage(COSObject obj, PDResources resources) {
		super(obj);
		this.resources = resources;
		parseJPXAndColorSpace();
	}

	@Override
	public ASAtom getType() {
		return ASAtom.IMAGE;
	}

	private void parseJPXAndColorSpace() {
		List<ASAtom> filters = getFilters();
		if (filters.contains(ASAtom.JPX_DECODE)) {
			this.jpxStream = JPEG2000.fromStream(getObject().getData());
			this.imageCS = this.imageCS == null ? this.jpxStream.getImageColorSpace() : this.imageCS;
		}
		COSObject rawColorSpace = getKey(ASAtom.COLORSPACE);
		if (rawColorSpace != null && !rawColorSpace.empty() && rawColorSpace.getType() != COSObjType.COS_NULL) {
			if (rawColorSpace.getType() == COSObjType.COS_NAME) {
				this.colorSpaceName = rawColorSpace.getName();
			}
			if (rawColorSpace.getType() == COSObjType.COS_ARRAY) {
				this.imageCS = ColorSpaceFactory.getColorSpace(rawColorSpace, resources);
			}
		}
	}

	public PDColorSpace getImageCS() {
		return this.imageCS;
	}

	public ASAtom getImageCSName() {
		return this.colorSpaceName;
	}

	public JPEG2000 getJPXStream() {
		return this.jpxStream;
	}

	public boolean isInterpolate() {
		Boolean value = getObject().getBooleanKey(ASAtom.INTERPOLATE);
		return value != null ? value.booleanValue() : false;
	}

	public List<PDXImage> getAlternates() {
		COSObject alternates = getKey(ASAtom.ALTERNATES);
		if (alternates != null && alternates.getType() == COSObjType.COS_ARRAY) {
			List<PDXImage> res = new ArrayList<>();
			for (COSObject alternate : ((COSArray) alternates.getDirectBase())) {
				if (alternate != null && alternate.getType() == COSObjType.COS_DICT) {
					COSObject image = alternate.getKey(ASAtom.IMAGE);
					if (image != null && image.getType() == COSObjType.COS_STREAM) {
						res.add(new PDXImage(image, resources));
					} else {
						LOGGER.log(Level.FINE, "Image key in alternate dictionary contains non stream value");
						return null;
					}
				} else {
					LOGGER.log(Level.FINE, "Alternates array contains non dictionary value");
				}
			}
			return Collections.unmodifiableList(res);
		}
		return Collections.emptyList();
	}

	public COSName getIntent() {
		COSObject object = getKey(ASAtom.INTENT);
		if (object != null && object.getType() == COSObjType.COS_NAME) {
			return (COSName) object.getDirectBase();
		}
		return null;
	}

	public Long getWidth() {
		return getObject().getIntegerKey(ASAtom.WIDTH);
	}

	public Long getHeight() {
		return getObject().getIntegerKey(ASAtom.HEIGHT);
	}

	public Long getBitsPerComponent() {
		return getObject().getIntegerKey(ASAtom.BITS_PER_COMPONENT);
	}

	public boolean getImageMask() {
		Boolean value = getObject().getBooleanKey(ASAtom.IMAGE_MASK);
		return value != null ? value.booleanValue() : false;
	}

	public PDXImage getMask() {
		COSObject object = getKey(ASAtom.MASK);
		if (object != null && object.getType() == COSObjType.COS_STREAM) {
			return new PDXImage(object, resources);
		}
		return null;
	}

	public Long getStructParent() {
		return getObject().getIntegerKey(ASAtom.STRUCT_PARENT);
	}

	public List<ASAtom> getFilters() {
		COSObject filters = getObject().getKey(ASAtom.FILTER);
		if (filters != null) {
			List<ASAtom> res = new ArrayList<>();
			if (filters.getType() == COSObjType.COS_NAME) {
				res.add(filters.getName());
			} else if (filters.getType() == COSObjType.COS_ARRAY) {
				for (COSObject filter : ((COSArray)filters.getDirectBase())) {
					if (filter == null || filter.getType() != COSObjType.COS_NAME) {
						LOGGER.log(Level.FINE, "Filter array contains non name value");
						return Collections.emptyList();
					}
					res.add(filter.getName());
				}
			}
			return Collections.unmodifiableList(res);
		}
		return Collections.emptyList();
	}

	public PDMetadata getMetadata() {
		COSObject object = getKey(ASAtom.METADATA);
		if (object != null && object.getType() == COSObjType.COS_STREAM) {
			return new PDMetadata(object);
		}
		return null;
	}

	public int getSMaskInData() {
		Long value = getObject().getIntegerKey(ASAtom.SMASK_IN_DATA);
		return value != null ? value.intValue() : 0;
	}

}
