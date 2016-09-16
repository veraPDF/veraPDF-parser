package org.verapdf.pd;

import org.verapdf.as.ASAtom;
import org.verapdf.cos.COSName;
import org.verapdf.cos.COSObject;
import org.verapdf.factory.colors.ColorSpaceFactory;
import org.verapdf.factory.fonts.PDFontFactory;
import org.verapdf.pd.colors.PDColorSpace;
import org.verapdf.pd.font.PDFont;
import org.verapdf.pd.images.PDXObject;
import org.verapdf.pd.patterns.PDShading;

/**
 * @author Timur Kamalov
 */
public class PDResources extends PDObject {

	public PDResources(COSObject resourcesDictionary) {
		super(resourcesDictionary);
	}

	//TODO : think about error cases
	public PDColorSpace getColorSpace(ASAtom name) {
		COSObject rawColorSpace = getResource(ASAtom.COLORSPACE, name);
		if (rawColorSpace != null) {
			return ColorSpaceFactory.getColorSpace(rawColorSpace);
		} else {
			return ColorSpaceFactory.getColorSpace(COSName.construct(name));
		}
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

	public PDColorSpace getPattern(ASAtom name) {
		COSObject rawPattern = getResource(ASAtom.PATTERN, name);
		return ColorSpaceFactory.getColorSpace(rawPattern);
	}

	public PDShading getShading(ASAtom name) {
		COSObject rawShading = getResource(ASAtom.SHADING, name);
		return new PDShading(rawShading);
	}

	public PDXObject getXObject(ASAtom name) {
		COSObject rawXObject = getResource(ASAtom.XOBJECT, name);
		return PDXObject.getTypedPDXObject(rawXObject);
	}

	public PDExtGState getExtGState(ASAtom name) {
		COSObject rawExtGState = getResource(ASAtom.EXT_G_STATE, name);
		return new PDExtGState(rawExtGState);
	}

	public PDFont getFont(ASAtom name) {
		COSObject rawFont = getResource(ASAtom.FONT, name);
		return PDFontFactory.getPDFont(rawFont);
	}

	private COSObject getResource(ASAtom type, ASAtom name) {
		COSObject dict = getObject().getKey(type);
		if (dict == null) {
			return null;
		}
		return dict.getKey(name);
	}


}
