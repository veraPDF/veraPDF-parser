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
package org.verapdf.pd.structure;

import org.verapdf.as.ASAtom;
import org.verapdf.cos.COSName;
import org.verapdf.cos.COSObjType;
import org.verapdf.cos.COSObject;
import org.verapdf.cos.COSString;
import org.verapdf.cos.COSKey;
import org.verapdf.parser.PDFFlavour;
import org.verapdf.tools.StaticResources;
import org.verapdf.tools.TaggedPDFConstants;
import org.verapdf.tools.TaggedPDFHelper;
import org.verapdf.tools.TaggedPDFRoleMapHelper;

/**
 * @author Maksim Bezrukov
 */
public class PDStructElem extends PDStructTreeNode {

	public PDStructElem(COSObject obj) {
		super(obj);
	}

	public ASAtom getType() {
		return getObject().getNameKey(ASAtom.TYPE);
	}

	public COSName getCOSStructureType() {
		COSObject object = getKey(ASAtom.S);
		if (object != null && object.getType() == COSObjType.COS_NAME) {
			return (COSName) object.getDirectBase();
		}
		return null;
	}

	public COSString getLang() {
		COSObject object = getKey(ASAtom.LANG);
		if (object != null && object.getType() == COSObjType.COS_STRING) {
			return (COSString) object.getDirectBase();
		}
		return null;
	}

	public PDStructureNameSpace getNameSpace() {
		COSObject object = getKey(ASAtom.NS);
		if (object != null && object.getType() == COSObjType.COS_DICT) {
			return PDStructureNameSpace.createNameSpace(object);
		}
		return null;
	}

	public StructureType getStructureType() {
		return StructureType.createStructureType(getKey(ASAtom.S), getKey(ASAtom.NS));
	}

	public COSObject getActualText() {
		return getKey(ASAtom.ACTUAL_TEXT);
	}

	public COSObject getRef() {
		return getKey(ASAtom.REF);
	}
	
	public boolean containsRef() {
		return knownKey(ASAtom.REF);
	}

	public String getAlternateDescription() {
		return getStringKey(ASAtom.ALT);
	}

	public String getExpandedAbbreviation() {
		return getStringKey(ASAtom.E);
	}

	public PDStructElem getParent() {
		COSObject parentObject = getKey(ASAtom.P);
		if (parentObject != null) {
			return new PDStructElem(parentObject);
		}
		return null;
	}

	public COSKey getPageObjectNumber() {
		COSObject object = getObject().getKey(ASAtom.PG);
		if (object != null) {
			return object.getKey();
		}
		return null;
	}

	public StructureType getDefaultStructureType() {
		return getDefaultStructureType(this.getStructureType());
	}

	public static StructureType getDefaultStructureType(StructureType structureType) {
		return TaggedPDFHelper.getDefaultStructureType(structureType);
	}
	
	public String getRoleMapToSameNamespaceTag() {
		return TaggedPDFHelper.getRoleMapToSameNamespaceTag(getStructureType());
	}

	public static StructureType getStructureElementStandardStructureType(PDStructElem pdStructElem) {
		return getStructureTypeStandardStructureType(pdStructElem.getStructureType());
	}

	public static StructureType getStructureTypeStandardStructureType(StructureType type) {
		PDFFlavour flavour = StaticResources.getFlavour();
		if (PDFFlavour.isFlavourPDFSpecification(flavour, PDFFlavour.PDFSpecification.ISO_32000_2_0)) {
			StructureType defaultStructureType = PDStructElem.getDefaultStructureType(type);
			if (defaultStructureType != null) {
				return defaultStructureType;
			}
		}
		if (!PDFFlavour.isFlavourPDFSpecification(flavour, PDFFlavour.PDFSpecification.ISO_32000_2_0) || PDFFlavour.isFlavourFamily(flavour, PDFFlavour.SpecificationFamily.WCAG)) {
			if (type != null) {
				return StructureType.createStructureType(ASAtom.getASAtom(
						StaticResources.getRoleMapHelper().getStandardType(type.getType())));
			}
		}
		return null;
	}

	public static String getStructureTypeStandardType(StructureType structureType) {
		StructureType type = getStructureTypeStandardStructureType(structureType);
		return type != null ? type.getType().getValue() : null;
	}

	public static String getStructureElementStandardType(PDStructElem pdStructElem) {
		StructureType type = getStructureElementStandardStructureType(pdStructElem);
		return type != null ? type.getType().getValue() : null;
	}

	public static boolean isStandardStructureType(StructureType type) {
		PDFFlavour flavour = StaticResources.getFlavour();
		boolean isStandard = false;
		if (PDFFlavour.isFlavourPDFSpecification(flavour, PDFFlavour.PDFSpecification.ISO_32000_2_0)) {
			isStandard = TaggedPDFHelper.isStandardType(type);
		}
		if (!PDFFlavour.isFlavourPDFSpecification(flavour, PDFFlavour.PDFSpecification.ISO_32000_2_0) || PDFFlavour.isFlavourFamily(flavour, PDFFlavour.SpecificationFamily.WCAG)) {
			if (type != null) {
				isStandard |= TaggedPDFRoleMapHelper.isStandardType(type);
			}
		}
		return isStandard;
	}

	public static boolean isMathStandardType(StructureType standardStructureType) {
		return PDFFlavour.isPDFUA2RelatedFlavour(StaticResources.getFlavour()) && standardStructureType != null &&
				TaggedPDFConstants.MATH_ML_NAMESPACE.equals(standardStructureType.getNameSpaceURI());
	}

	public static boolean isPassThroughTag(String structureType) {
		return TaggedPDFConstants.NON_STRUCT.equals(structureType) || TaggedPDFConstants.DIV.equals(structureType) ||
				TaggedPDFConstants.PART.equals(structureType);
	}
}
