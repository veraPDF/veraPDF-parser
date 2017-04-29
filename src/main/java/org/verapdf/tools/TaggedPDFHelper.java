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
import org.verapdf.cos.COSObjType;
import org.verapdf.cos.COSObject;
import org.verapdf.pd.structure.PDStructElem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Maksim Bezrukov
 */
public class TaggedPDFHelper {

	private static final Logger LOGGER = Logger.getLogger(TaggedPDFHelper.class.getCanonicalName());

	private static final int MAX_NUMBER_OF_ELEMENTS = 1;

	private TaggedPDFHelper() {
		// disable default constructor
	}

	public static List<PDStructElem> getStructTreeRootChildren(COSObject parent) {
		return getChildren(parent, false);
	}

	public static List<PDStructElem> getStructElemChildren(COSObject parent) {
		return getChildren(parent, true);
	}

	/**
	 * Get all structure elements for current dictionary
	 *
	 * @param parent parent dictionary
	 * @return list of structure elements
	 */
	private static List<PDStructElem> getChildren(COSObject parent, boolean checkType) {
		if (parent == null || parent.getType() != COSObjType.COS_DICT) {
			LOGGER.log(Level.FINE, "Parent element for struct elements is null or not a COSDictionary");
			return Collections.emptyList();
		}

		COSObject children = parent.getKey(ASAtom.K);
		if (children != null) {
			if (children.getType() == COSObjType.COS_DICT && isStructElem(children, checkType)) {
				List<PDStructElem> list = new ArrayList<>(MAX_NUMBER_OF_ELEMENTS);
				list.add(new PDStructElem(children));
				return Collections.unmodifiableList(list);
			} else if (children.getType() == COSObjType.COS_ARRAY) {
				return getChildrenFromArray(children, checkType);
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
	private static List<PDStructElem> getChildrenFromArray(COSObject children, boolean checkType) {
		if (children.size().intValue() > 0) {
			List<PDStructElem> list = new ArrayList<>();
			for (int i = 0; i < children.size().intValue(); ++i) {
				COSObject elem = children.at(i);
				if (elem.getType() == COSObjType.COS_DICT && isStructElem(elem, checkType)) {
					list.add(new PDStructElem(elem));
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
