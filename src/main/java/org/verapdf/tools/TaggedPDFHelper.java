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
package org.verapdf.tools;

import org.verapdf.as.ASAtom;
import org.verapdf.cos.COSKey;
import org.verapdf.cos.COSObjType;
import org.verapdf.cos.COSObject;
import org.verapdf.pd.structure.PDNameSpaceRoleMapping;
import org.verapdf.pd.structure.PDStructElem;
import org.verapdf.pd.structure.PDStructureNameSpace;
import org.verapdf.pd.structure.StructureType;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Maksim Bezrukov
 */
public class TaggedPDFHelper {

	private static final Logger LOGGER = Logger.getLogger(TaggedPDFHelper.class.getCanonicalName());

	public static final String PDF_NAMESPACE = "http://iso.org/pdf/ssn";
	public static final String PDF2_NAMESPACE = "http://iso.org/pdf2/ssn";
	public static final String MATH_ML_NAMESPACE = "http://www.w3.org/1998/Math/MathML";

	private static Set<String> PDF_1_7_STANDART_ROLE_TYPES;
	private static Set<String> PDF_2_0_STANDART_ROLE_TYPES;

	static {
		Set<String> tempSet = new HashSet<>();
		// Common standard structure types for PDF 1.7 and 2.0
		tempSet.add("Document");
		tempSet.add("Part");
		tempSet.add("Div");
		tempSet.add("Caption");
		tempSet.add("THead");
		tempSet.add("TBody");
		tempSet.add("TFoot");
		tempSet.add("H");
		tempSet.add("P");
		tempSet.add("L");
		tempSet.add("LI");
		tempSet.add("Lbl");
		tempSet.add("LBody");
		tempSet.add("Table");
		tempSet.add("TR");
		tempSet.add("TH");
		tempSet.add("TD");
		tempSet.add("Span");
		tempSet.add("Link");
		tempSet.add("Annot");
		tempSet.add("Ruby");
		tempSet.add("Warichu");
		tempSet.add("Figure");
		tempSet.add("Formula");
		tempSet.add("Form");
		tempSet.add("RB");
		tempSet.add("RT");
		tempSet.add("RP");
		tempSet.add("WT");
		tempSet.add("WP");

		Set<String> pdf_1_7 = new HashSet<>(tempSet);

		// Standart structure types present in 1.7
		pdf_1_7.add("Art");
		pdf_1_7.add("Sect");
		pdf_1_7.add("BlockQuote");
		pdf_1_7.add("TOC");
		pdf_1_7.add("TOCI");
		pdf_1_7.add("Index");
		pdf_1_7.add("NonStruct");
		pdf_1_7.add("Private");
		pdf_1_7.add("Quote");
		pdf_1_7.add("Note");
		pdf_1_7.add("Reference");
		pdf_1_7.add("BibEntry");
		pdf_1_7.add("Code");
		tempSet.add("H1");
		tempSet.add("H2");
		tempSet.add("H3");
		tempSet.add("H4");
		tempSet.add("H5");
		tempSet.add("H6");

		Set<String> pdf_2_0 = new HashSet<>(tempSet);

		pdf_2_0.add("DocumentFragment");
		pdf_2_0.add("Aside");
		pdf_2_0.add("Title");
		pdf_2_0.add("FENote");
		pdf_2_0.add("Sub");
		pdf_2_0.add("Em");
		pdf_2_0.add("Strong");
		pdf_2_0.add("Artifact");

		PDF_1_7_STANDART_ROLE_TYPES = Collections.unmodifiableSet(pdf_1_7);
		PDF_2_0_STANDART_ROLE_TYPES = Collections.unmodifiableSet(pdf_2_0);
	}

	private static final int MAX_NUMBER_OF_ELEMENTS = 1;
	private static Map<ASAtom, Set<COSKey>> visitedWithNS = new HashMap<>();
	private static Set<ASAtom> visitedWithoutNS = new HashSet<>();

	private TaggedPDFHelper() {
		// disable default constructor
	}

	public static StructureType getDefaultStructureType(StructureType type, Map<ASAtom, ASAtom> rootRoleMap) {
		visitedWithNS.clear();
		visitedWithoutNS.clear();
		addVisited(type);
		StructureType curr = getEquivalent(type, rootRoleMap);
		if (curr == null || isVisited(curr)) {
			return isStandardType(type) ? type : null;
		}
		while (curr != null && !isVisited(curr)) {
			if (isStandardType(curr)) {
				return curr;
			}
			addVisited(curr);
			curr = getEquivalent(curr, rootRoleMap);
		}
		return null;
	}

	private static StructureType getEquivalent(StructureType type, Map<ASAtom, ASAtom> rootRoleMap) {
		PDStructureNameSpace nameSpace = type.getNameSpace();
		if (nameSpace != null) {
			PDNameSpaceRoleMapping nameSpaceMapping = nameSpace.getNameSpaceMapping();
			if (nameSpaceMapping != null) {
				return nameSpaceMapping.getEquivalentType(type.getType());
			} else if (!PDF_NAMESPACE.equals(nameSpace.getNS())) {
				return null;
			}
		}
		ASAtom equiv = rootRoleMap.get(type.getType());
		return equiv == null ? null : StructureType.createStructureType(equiv);
	}

	private static boolean isStandardType(StructureType type) {
		String structureType = type.getType().getValue();
		PDStructureNameSpace nameSpace = type.getNameSpace();
		if (nameSpace != null) {
			switch (nameSpace.getNS()) {
				case PDF_NAMESPACE:
					return PDF_1_7_STANDART_ROLE_TYPES.contains(structureType);
				case PDF2_NAMESPACE:
					return PDF_2_0_STANDART_ROLE_TYPES.contains(structureType) || structureType.matches("^H[1-9][0-9]*$");
				case MATH_ML_NAMESPACE:
					return true;
				default:
					return false;
			}
		} else {
			return PDF_1_7_STANDART_ROLE_TYPES.contains(structureType);
		}
	}

	private static void addVisited(StructureType type) {
		ASAtom structType = type.getType();
		PDStructureNameSpace nameSpace = type.getNameSpace();
		if (nameSpace != null) {
			COSKey key = nameSpace.getObject().getObjectKey();
			Set<COSKey> nameSpaces;
			if (visitedWithNS.containsKey(structType)) {
				nameSpaces = visitedWithNS.get(structType);
			} else {
				nameSpaces = new HashSet<>();
				visitedWithNS.put(structType, nameSpaces);
			}
			nameSpaces.add(key);
		} else {
			visitedWithoutNS.add(structType);
		}
	}

	private static boolean isVisited(StructureType type) {
		ASAtom structType = type.getType();
		PDStructureNameSpace nameSpace = type.getNameSpace();
		if (nameSpace != null) {
			if (visitedWithNS.containsKey(structType)) {
				Set<COSKey> nameSpaces = visitedWithNS.get(structType);
				COSKey key = nameSpace.getObject().getObjectKey();
				return nameSpaces.contains(key);
			} else {
				return false;
			}
		} else {
			return visitedWithoutNS.contains(structType);
		}
	}

	public static List<PDStructElem> getStructTreeRootChildren(COSObject parent, Map<ASAtom, ASAtom> roleMap) {
		return getChildren(parent, roleMap, false);
	}

	public static List<PDStructElem> getStructElemChildren(COSObject parent, Map<ASAtom, ASAtom> roleMap) {
		return getChildren(parent, roleMap, true);
	}

	/**
	 * Get all structure elements for current dictionary
	 *
	 * @param parent parent dictionary
	 * @return list of structure elements
	 */
	private static List<PDStructElem> getChildren(COSObject parent, Map<ASAtom, ASAtom> roleMap, boolean checkType) {
		if (parent == null || parent.getType() != COSObjType.COS_DICT) {
			LOGGER.log(Level.FINE, "Parent element for struct elements is null or not a COSDictionary");
			return Collections.emptyList();
		}

		COSObject children = parent.getKey(ASAtom.K);
		if (children != null) {
			if (children.getType() == COSObjType.COS_DICT && isStructElem(children, checkType)) {
				List<PDStructElem> list = new ArrayList<>(MAX_NUMBER_OF_ELEMENTS);
				list.add(new PDStructElem(children, roleMap));
				return Collections.unmodifiableList(list);
			} else if (children.getType() == COSObjType.COS_ARRAY) {
				return getChildrenFromArray(children, roleMap, checkType);
			}
		}
		return Collections.emptyList();
	}

	/**
	 * Transform array of dictionaries to list of structure elements
	 *
	 * @param children array of children structure elements
	 * @return list of structure elements
	 */
	private static List<PDStructElem> getChildrenFromArray(COSObject children, Map<ASAtom, ASAtom> roleMap, boolean checkType) {
		if (children.size().intValue() > 0) {
			List<PDStructElem> list = new ArrayList<>();
			for (int i = 0; i < children.size().intValue(); ++i) {
				COSObject elem = children.at(i);
				if (elem.getType() == COSObjType.COS_DICT && isStructElem(elem, checkType)) {
					list.add(new PDStructElem(elem, roleMap));
				}
			}
			return Collections.unmodifiableList(list);
		}
		return Collections.emptyList();
	}

	private static boolean isStructElem(COSObject dictionary, boolean checkType) {
		if (dictionary == null || dictionary.getType() != COSObjType.COS_DICT) {
			return false;
		}
		ASAtom type = dictionary.getNameKey(ASAtom.TYPE);
		return !checkType || type == null || type.equals(ASAtom.STRUCT_ELEM);
	}
}
