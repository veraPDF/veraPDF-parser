package org.verapdf.pd;

import org.verapdf.as.ASAtom;
import org.verapdf.cos.COSName;
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

	public PDResources(COSObject resourcesDictionary) {
		super(resourcesDictionary);
	}

	//TODO : think about error cases
	public PDColorSpace getColorSpace(ASAtom name) {
		if (colorSpaceMap.containsKey(name)) {
			return colorSpaceMap.get(name);
		}
		PDColorSpace colorSpace;
		COSObject rawColorSpace = getResource(ASAtom.COLORSPACE, name);
		if (rawColorSpace != null && !rawColorSpace.empty()) {
			colorSpace = ColorSpaceFactory.getColorSpace(rawColorSpace);
		} else {
			colorSpace = ColorSpaceFactory.getColorSpace(COSName.construct(name));
		}
		colorSpaceMap.put(name, colorSpace);
		return colorSpace;
	}

	public PDColorSpace getDefaultColorSpace(ASAtom name) {
		ASAtom defaultName = ColorSpaceFactory.getDefaultValue(this, name);
		if (hasColorSpace(defaultName)) {
			return getColorSpace(defaultName);
		}
		return null;
	}

	public boolean hasColorSpace(ASAtom name) {
		COSObject colorSpace = getResource(ASAtom.COLORSPACE, name);
		if (colorSpace != null && !colorSpace.empty()) {
			return true;
		}
		return false;
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
		PDShading shading = new PDShading(rawShading);
		shadingMap.put(name, shading);
		return shading;
	}

	public PDXObject getXObject(ASAtom name) {
		if (xObjectMap.containsKey(name)) {
			return xObjectMap.get(name);
		}
		COSObject rawXObject = getResource(ASAtom.XOBJECT, name);
		PDXObject pdxObject = PDXObject.getTypedPDXObject(rawXObject);
		xObjectMap.put(name, pdxObject);
		return pdxObject;
	}

	public PDExtGState getExtGState(ASAtom name) {
		if (extGStateMap.containsKey(name)) {
			return extGStateMap.get(name);
		}
		COSObject rawExtGState = getResource(ASAtom.EXT_G_STATE, name);
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

	private Set<ASAtom> getNames(ASAtom type) {
		COSObject dict = getKey(type);
		if (dict != null) {
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
