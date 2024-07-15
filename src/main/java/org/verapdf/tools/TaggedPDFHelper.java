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
import org.verapdf.pd.structure.*;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Maksim Bezrukov
 */
public class TaggedPDFHelper {

	private static final Logger LOGGER = Logger.getLogger(TaggedPDFHelper.class.getCanonicalName());

	private static final Set<String> PDF_1_4_STANDARD_ROLE_TYPES;
	private static final Set<String> PDF_1_7_STANDARD_ROLE_TYPES;
	private static final Set<String> PDF_2_0_STANDARD_ROLE_TYPES;
	private static final Set<String> WCAG_STANDARD_ROLE_TYPES;

	static {
		Set<String> tempSet = new HashSet<>();

		// Common standard structure types for PDF 1.4, 1.7 and 2.0

		// Standard structure types for grouping elements PDF 1.4, 1.7 and 2.0
		tempSet.add(TaggedPDFConstants.DOCUMENT);
		tempSet.add(TaggedPDFConstants.PART);
		tempSet.add(TaggedPDFConstants.DIV);
		tempSet.add(TaggedPDFConstants.CAPTION);
		tempSet.add(TaggedPDFConstants.SECT);
		tempSet.add(TaggedPDFConstants.NON_STRUCT);

		// Standard structure types for paragraphlike elements PDF 1.4, 1.7 and 2.0
		tempSet.add(TaggedPDFConstants.H);
		tempSet.add(TaggedPDFConstants.P);

		//Standard structure types for list elements PDF 1.4, 1.7 and 2.0
		tempSet.add(TaggedPDFConstants.L);
		tempSet.add(TaggedPDFConstants.LI);
		tempSet.add(TaggedPDFConstants.LBL);
		tempSet.add(TaggedPDFConstants.LBODY);

		//Standard structure types for table elements PDF 1.4, 1.7 and 2.0
		tempSet.add(TaggedPDFConstants.TABLE);
		tempSet.add(TaggedPDFConstants.TR);
		tempSet.add(TaggedPDFConstants.TH);
		tempSet.add(TaggedPDFConstants.TD);

		// Standard structure types for inline-level structure elements PDF 1.4, 1.7 and 2.0
		tempSet.add(TaggedPDFConstants.SPAN);
		tempSet.add(TaggedPDFConstants.LINK);

		// Standard structure types for illustration elements PDF 1.4, 1.7 and 2.0
		tempSet.add(TaggedPDFConstants.FIGURE);
		tempSet.add(TaggedPDFConstants.FORMULA);
		tempSet.add(TaggedPDFConstants.FORM);

		Set<String> pdf_1_7 = new HashSet<>(tempSet);

		// Standard structure types present in PDF 1.7 and 1.4

		// Standard structure types for grouping elements PDF 1.7 and 1.4
		pdf_1_7.add(TaggedPDFConstants.ART);
		pdf_1_7.add(TaggedPDFConstants.BLOCK_QUOTE);
		pdf_1_7.add(TaggedPDFConstants.TOC);
		pdf_1_7.add(TaggedPDFConstants.TOCI);
		pdf_1_7.add(TaggedPDFConstants.INDEX);
		pdf_1_7.add(TaggedPDFConstants.PRIVATE);

		// Standard structure types for inline-level structure elements PDF 1.7 and 1.4
		pdf_1_7.add(TaggedPDFConstants.QUOTE);
		pdf_1_7.add(TaggedPDFConstants.NOTE);
		pdf_1_7.add(TaggedPDFConstants.REFERENCE);
		pdf_1_7.add(TaggedPDFConstants.BIB_ENTRY);
		pdf_1_7.add(TaggedPDFConstants.CODE);

		// Standard structure types for paragraphlike elements PDF 1.7 and 1.4
		pdf_1_7.add(TaggedPDFConstants.H1);
		pdf_1_7.add(TaggedPDFConstants.H2);
		pdf_1_7.add(TaggedPDFConstants.H3);
		pdf_1_7.add(TaggedPDFConstants.H4);
		pdf_1_7.add(TaggedPDFConstants.H5);
		pdf_1_7.add(TaggedPDFConstants.H6);

		Set<String> pdf_1_4 = new HashSet<>(pdf_1_7);
		Set<String> pdf_2_0 = new HashSet<>(tempSet);
		tempSet = new HashSet<>();

		// Common standard structure types for PDF 1.7 and 2.0
		tempSet.add(TaggedPDFConstants.THEAD);
		tempSet.add(TaggedPDFConstants.TBODY);
		tempSet.add(TaggedPDFConstants.TFOOT);
		tempSet.add(TaggedPDFConstants.ANNOT);
		tempSet.add(TaggedPDFConstants.RUBY);
		tempSet.add(TaggedPDFConstants.WARICHU);
		tempSet.add(TaggedPDFConstants.RB);
		tempSet.add(TaggedPDFConstants.RT);
		tempSet.add(TaggedPDFConstants.RP);
		tempSet.add(TaggedPDFConstants.WT);
		tempSet.add(TaggedPDFConstants.WP);

		pdf_1_7.addAll(tempSet);
		pdf_2_0.addAll(tempSet);

		Set<String> wcag = new HashSet<>(pdf_1_7);
		wcag.add(TaggedPDFConstants.ARTIFACT);
		wcag.add(TaggedPDFConstants.TITLE);

		pdf_2_0.add(TaggedPDFConstants.DOCUMENT_FRAGMENT);
		pdf_2_0.add(TaggedPDFConstants.ASIDE);
		pdf_2_0.add(TaggedPDFConstants.TITLE);
		pdf_2_0.add(TaggedPDFConstants.FENOTE);
		pdf_2_0.add(TaggedPDFConstants.SUB);
		pdf_2_0.add(TaggedPDFConstants.EM);
		pdf_2_0.add(TaggedPDFConstants.STRONG);
		pdf_2_0.add(TaggedPDFConstants.ARTIFACT);

		PDF_1_4_STANDARD_ROLE_TYPES = Collections.unmodifiableSet(pdf_1_4);
		PDF_1_7_STANDARD_ROLE_TYPES = Collections.unmodifiableSet(pdf_1_7);
		PDF_2_0_STANDARD_ROLE_TYPES = Collections.unmodifiableSet(pdf_2_0);
		WCAG_STANDARD_ROLE_TYPES = Collections.unmodifiableSet(wcag);
	}

	private static final int MAX_NUMBER_OF_ELEMENTS = 1;
	private static final Map<ASAtom, Set<COSKey>> visitedWithNS = new HashMap<>();
	private static final Set<ASAtom> visitedWithoutNS = new HashSet<>();

	private TaggedPDFHelper() {
		// disable default constructor
	}

	public static StructureType getDefaultStructureType(StructureType type) {
		if (type == null) {
			return null;
		}
		visitedWithNS.clear();
		visitedWithoutNS.clear();
		addVisited(type);
		StructureType curr = getEquivalent(type, StaticResources.getRoleMapHelper().getRoleMap());
		if (curr == null || isVisited(curr)) {
			return isStandardType(type) ? type : null;
		}
		while (curr != null && !isVisited(curr)) {
			if (isStandardType(curr)) {
				return curr;
			}
			addVisited(curr);
			curr = getEquivalent(curr, StaticResources.getRoleMapHelper().getRoleMap());
		}
		return null;
	}

	public static String getRoleMapToSameNamespaceTag(StructureType type) {
		if (type == null) {
			return null;
		}
		visitedWithNS.clear();
		visitedWithoutNS.clear();
		addVisited(type);
		StructureType prev = type;
		StructureType curr = getEquivalent(prev, Collections.emptyMap());
		Map<String, ASAtom> processedTypes = new HashMap<>();
		while (curr != null) {
			if (prev.getNameSpaceURI() != null) {
				processedTypes.put(prev.getNameSpaceURI(), prev.getType());
			}
			if (curr.getNameSpaceURI() != null && processedTypes.containsKey(curr.getNameSpaceURI())) {
				ASAtom processedType = processedTypes.get(curr.getNameSpaceURI());
				if (curr.getType() != null && !Objects.equals(curr.getType(), processedType)) {
					return curr.getNameSpaceURI() + ":" + (processedType != null ? processedType.getValue() : null);
				}
				return null;
			}
			if (isVisited(curr)) {
				return null;
			}
			addVisited(curr);
			prev = curr;
			curr = getEquivalent(prev, Collections.emptyMap());
		}
		return null;
	}

	public static Boolean isCircularMappingExist(StructureType type) {
		if (type == null) {
			return null;
		}
		visitedWithNS.clear();
		visitedWithoutNS.clear();
		addVisited(type);
		StructureType prev = type;
		StructureType curr = getEquivalent(prev, Collections.emptyMap());
		Map<String, ASAtom> processedTypes = new HashMap<>();
		while (curr != null) {
			if (prev.getNameSpaceURI() != null) {
				processedTypes.put(prev.getNameSpaceURI(), prev.getType());
			}
			if (curr.getNameSpaceURI() != null && processedTypes.containsKey(curr.getNameSpaceURI())) {
				ASAtom processedType = processedTypes.get(curr.getNameSpaceURI());
				if (curr.getType() != null && Objects.equals(curr.getType(), processedType)) {
					return true;
				}
			}
			if (isVisited(curr)) {
				return false;
			}
			addVisited(curr);
			prev = curr;
			curr = getEquivalent(prev, Collections.emptyMap());
		}
		return false;
	}
	
	private static StructureType getEquivalent(StructureType type, Map<ASAtom, ASAtom> roleMap) {
		PDStructureNameSpace nameSpace = type.getNameSpace();
		if (nameSpace != null) {
			PDNameSpaceRoleMapping nameSpaceMapping = nameSpace.getNameSpaceMapping();
			if (nameSpaceMapping != null) {
				return nameSpaceMapping.getEquivalentType(type.getType());
			} else if (!TaggedPDFConstants.PDF_NAMESPACE.equals(nameSpace.getNS())) {
				return null;
			}
		}
		ASAtom equiv = roleMap.get(type.getType());
		return equiv == null ? null : StructureType.createStructureType(equiv);
	}

	public static boolean isStandardType(StructureType type) {
		String structureType = type.getType().getValue();
		PDStructureNameSpace nameSpace = type.getNameSpace();
		if (nameSpace != null && nameSpace.getNS() != null) {
			switch (nameSpace.getNS()) {
				case TaggedPDFConstants.PDF_NAMESPACE:
					return PDF_1_7_STANDARD_ROLE_TYPES.contains(structureType);
				case TaggedPDFConstants.PDF2_NAMESPACE:
					return PDF_2_0_STANDARD_ROLE_TYPES.contains(structureType)
							|| structureType.matches(TaggedPDFConstants.HN_REGEXP);
				case TaggedPDFConstants.MATH_ML_NAMESPACE:
					return true;
				default:
					return false;
			}
		} else {
			return PDF_1_7_STANDARD_ROLE_TYPES.contains(structureType);
		}
	}

	public static boolean isWCAGStandardType(StructureType type) {
		String structureType = type.getType().getValue();
		return WCAG_STANDARD_ROLE_TYPES.contains(structureType);
	}

	private static void addVisited(StructureType type) {
		ASAtom structType = type.getType();
		PDStructureNameSpace nameSpace = type.getNameSpace();
		if (nameSpace != null) {
			Set<COSKey> nameSpaces;
			if (visitedWithNS.containsKey(structType)) {
				nameSpaces = visitedWithNS.get(structType);
			} else {
				nameSpaces = new HashSet<>();
				visitedWithNS.put(structType, nameSpaces);
			}
			COSKey key = nameSpace.getObject().getObjectKey();
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

	public static List<PDStructElem> getStructNodeStructChildren(COSObject parent) {
		return getStructChildren(parent, true);
	}

	public static List<Object> getStructNodeChildren(COSObject parent) {
		return getChildren(parent, true);
	}

	private static List<Object> getChildren(COSObject parent, boolean checkType) {
		if (parent == null || parent.getType() != COSObjType.COS_DICT) {
			LOGGER.log(Level.FINE, "Parent element for struct elements is null or not a COSDictionary");
			return Collections.emptyList();
		}

		COSObject children = parent.getKey(ASAtom.K);
		if (children != null) {
			if (isStructElem(children, checkType)) {
				List<Object> list = new ArrayList<>(MAX_NUMBER_OF_ELEMENTS);
				list.add(new PDStructElem(children));
				return Collections.unmodifiableList(list);
			} else if (isMCR(children)) {
				List<Object> list = new ArrayList<>(MAX_NUMBER_OF_ELEMENTS);
				list.add(new PDMCRDictionary(children));
				return Collections.unmodifiableList(list);
			} else if (children.getType() == COSObjType.COS_INTEGER) {
				List<Object> list = new ArrayList<>(MAX_NUMBER_OF_ELEMENTS);
				list.add(children);
				return Collections.unmodifiableList(list);
			} else if (children.getType() == COSObjType.COS_ARRAY) {
				return getChildrenFromArray(children, checkType);
			} else if (isOBJR(children)) {
				List<Object> list = new ArrayList<>(MAX_NUMBER_OF_ELEMENTS);
				list.add(new PDOBJRDictionary(children));
				return Collections.unmodifiableList(list);
			}
		}
		return Collections.emptyList();
	}

	/**
	 * Get all structure elements for current dictionary
	 *
	 * @param parent parent dictionary
	 * @return list of structure elements
	 */
	private static List<PDStructElem> getStructChildren(COSObject parent, boolean checkType) {
		if (parent == null || parent.getType() != COSObjType.COS_DICT) {
			LOGGER.log(Level.FINE, "Parent element for struct elements is null or not a COSDictionary");
			return Collections.emptyList();
		}

		COSObject children = parent.getKey(ASAtom.K);
		if (children != null) {
			if (isStructElem(children, checkType)) {
				List<PDStructElem> list = new ArrayList<>(MAX_NUMBER_OF_ELEMENTS);
				list.add(new PDStructElem(children));
				return Collections.unmodifiableList(list);
			} else if (children.getType() == COSObjType.COS_ARRAY) {
				return getStructChildrenFromArray(children, checkType);
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
	private static List<PDStructElem> getStructChildrenFromArray(COSObject children, boolean checkType) {
		if (children.size() > 0) {
			List<PDStructElem> list = new ArrayList<>();
			for (int i = 0; i < children.size(); ++i) {
				COSObject elem = children.at(i);
				if (isStructElem(elem, checkType)) {
					list.add(new PDStructElem(elem));
				}
			}
			return Collections.unmodifiableList(list);
		}
		return Collections.emptyList();
	}

	private static List<Object> getChildrenFromArray(COSObject children, boolean checkType) {
		if (children.size() > 0) {
			List<Object> list = new ArrayList<>();
			for (int i = 0; i < children.size(); ++i) {
				COSObject elem = children.at(i);
				if (isStructElem(elem, checkType)) {
					list.add(new PDStructElem(elem));
				} else if (isMCR(elem)) {
					list.add(new PDMCRDictionary(elem));
				} else if (elem.getType() == COSObjType.COS_INTEGER) {
					list.add(elem);
				} else if (isOBJR(elem)) {
					list.add(new PDOBJRDictionary(elem));
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

	public static boolean isMCR(COSObject obj) {
		if (obj == null || obj.empty()) {
			return false;
		}
		if (!obj.getType().isDictionaryBased()) {
			return false;
		}
		ASAtom type = obj.getNameKey(ASAtom.TYPE);
		return ASAtom.MCR.equals(type);
	}

	public static boolean isOBJR(COSObject obj) {
		if (obj == null || obj.empty()) {
			return false;
		}
		if (!obj.getType().isDictionaryBased()) {
			return false;
		}
		ASAtom type = obj.getNameKey(ASAtom.TYPE);
		return ASAtom.OBJR.equals(type);
	}

	public static boolean isContentItem(COSObject obj) {
		if (obj == null || obj.empty()) {
			return false;
		}
		if (obj.getType() == COSObjType.COS_INTEGER) {
			return true;
		}
		if (!obj.getType().isDictionaryBased()) {
			return false;
		}
		ASAtom type = obj.getNameKey(ASAtom.TYPE);
		return type == ASAtom.MCR || type == ASAtom.OBJR;
	}

	public static Set<String> getPdf14StandardRoleTypes() {
		return PDF_1_4_STANDARD_ROLE_TYPES;
	}

	public static Set<String> getPdf17StandardRoleTypes() {
		return PDF_1_7_STANDARD_ROLE_TYPES;
	}

	public static Set<String> getPdf20StandardRoleTypes() {
		return PDF_2_0_STANDARD_ROLE_TYPES;
	}

	public static Set<String> getWcagStandardRoleTypes() {
		return WCAG_STANDARD_ROLE_TYPES;
	}
}
