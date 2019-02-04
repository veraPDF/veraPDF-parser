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
import org.verapdf.cos.COSName;
import org.verapdf.cos.COSObjType;
import org.verapdf.cos.COSObject;
import org.verapdf.factory.colors.ColorSpaceFactory;
import org.verapdf.factory.fonts.PDFontFactory;
import org.verapdf.pd.colors.PDColorSpace;
import org.verapdf.pd.font.PDFont;
import org.verapdf.pd.images.PDXObject;
import org.verapdf.pd.patterns.PDPattern;
import org.verapdf.pd.patterns.PDShading;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author Timur Kamalov
 */
public class PDResources extends PDObject {

	private Map<ASAtom, PDColorSpace> colorSpaceMap = new HashMap<>();
	private Map<ASAtom, PDPattern> patternMap = new HashMap<>();
	private Map<ASAtom, PDShading> shadingMap = new HashMap<>();
	private Map<ASAtom, PDXObject> xObjectMap = new HashMap<>();
	private Map<ASAtom, PDExtGState> extGStateMap = new HashMap<>();
	private Map<ASAtom, PDFont> fontMap = new HashMap<>();
	private Map<ASAtom, PDResource> propertiesMap = new HashMap<>();

	public PDResources(COSObject resourcesDictionary) {
		super(resourcesDictionary);
	}

	public PDColorSpace getColorSpace(ASAtom name) {
		return this.getColorSpace(name, false);
	}

	//TODO : think about error cases
	public PDColorSpace getColorSpace(ASAtom name, boolean isDefault) {
		if (colorSpaceMap.containsKey(name)) {
			return colorSpaceMap.get(name);
		}
		PDColorSpace colorSpace;
		COSObject rawColorSpace = getResource(ASAtom.COLORSPACE, name);
		if (rawColorSpace != null && !rawColorSpace.empty()) {
			colorSpace = ColorSpaceFactory.getColorSpace(rawColorSpace, this, isDefault);
		} else {
			colorSpace = ColorSpaceFactory.getColorSpace(COSName.construct(name), this, isDefault);
		}
		colorSpaceMap.put(name, colorSpace);
		return colorSpace;
	}

	public PDColorSpace getDefaultColorSpace(ASAtom name) {
		ASAtom defaultName = ColorSpaceFactory.getDefaultValue(this, name);
		if (hasColorSpace(defaultName)) {
			return getColorSpace(defaultName, true);
		}
		return null;
	}

	public boolean hasColorSpace(ASAtom name) {
		COSObject colorSpace = getResource(ASAtom.COLORSPACE, name);
		return colorSpace != null && !colorSpace.empty();
	}

	public PDPattern getPattern(ASAtom name) {
		if (patternMap.containsKey(name)) {
			return patternMap.get(name);
		}
		COSObject rawPattern = getResource(ASAtom.PATTERN, name);
		PDColorSpace cs = ColorSpaceFactory.getColorSpace(rawPattern);
		if (cs != null && ASAtom.PATTERN.equals(cs.getType())) {
			PDPattern pattern = (PDPattern) cs;
			patternMap.put(name, pattern);
			return pattern;
		} else {
			patternMap.put(name, null);
			return null;
		}
	}

	public PDShading getShading(ASAtom name) {
		if (shadingMap.containsKey(name)) {
			return shadingMap.get(name);
		}
		COSObject rawShading = getResource(ASAtom.SHADING, name);
		if (rawShading == null || rawShading.empty()) {
			return null;
		}
		PDShading shading = new PDShading(rawShading, this);
		shadingMap.put(name, shading);
		return shading;
	}

	public PDXObject getXObject(ASAtom name) {
		if (xObjectMap.containsKey(name)) {
			return xObjectMap.get(name);
		}
		COSObject rawXObject = getResource(ASAtom.XOBJECT, name);
		PDXObject pdxObject = PDXObject.getTypedPDXObject(rawXObject, this);
		xObjectMap.put(name, pdxObject);
		return pdxObject;
	}

	public PDExtGState getExtGState(ASAtom name) {
		if (extGStateMap.containsKey(name)) {
			return extGStateMap.get(name);
		}
		COSObject rawExtGState = getResource(ASAtom.EXT_G_STATE, name);
		if (rawExtGState == null || rawExtGState.empty()) {
			return null;
		}
		PDExtGState extGState = new PDExtGState(rawExtGState);
		extGStateMap.put(name, extGState);
		return extGState;
	}

	public PDFont getFont(ASAtom name) {
		if (fontMap.containsKey(name)) {
			return fontMap.get(name);
		}
		COSObject rawFont = getResource(ASAtom.FONT, name);
		PDFont font = PDFontFactory.getPDFont(rawFont);
		fontMap.put(name, font);
		return font;
	}

	public PDResource getProperties(ASAtom name) {
		if (propertiesMap.containsKey(name)) {
			return propertiesMap.get(name);
		}
		COSObject rawProperties = getResource(ASAtom.PROPERTIES, name);
		if (rawProperties == null || rawProperties.empty()) {
			return null;
		}
		PDResource properties = new PDResource(rawProperties);
		propertiesMap.put(name, properties);
		return properties;
	}

	public Set<ASAtom> getExtGStateNames() {
		return getNames(ASAtom.EXT_G_STATE);
	}

	public Set<ASAtom> getColorSpaceNames() {
		return getNames(ASAtom.COLORSPACE);
	}

	public Set<ASAtom> getPatternNames() {
		return getNames(ASAtom.PATTERN);
	}

	public Set<ASAtom> getShadingNames() {
		return getNames(ASAtom.SHADING);
	}

	public Set<ASAtom> getXObjectNames() {
		return getNames(ASAtom.XOBJECT);
	}

	public Set<ASAtom> getFontNames() {
		return getNames(ASAtom.FONT);
	}

	public Set<ASAtom> getPropertiesNames() {
		return getNames(ASAtom.PROPERTIES);
	}

	private Set<ASAtom> getNames(ASAtom type) {
		COSObject dict = getKey(type);
		if (dict != null && dict.getType() == COSObjType.COS_DICT) {
			return dict.getKeySet();
		}
		return Collections.emptySet();
	}

	private COSObject getResource(ASAtom type, ASAtom name) {
		COSObject dict = getKey(type);
		if (dict != null) {
			return dict.getKey(name);
		}
		return null;
	}


}
