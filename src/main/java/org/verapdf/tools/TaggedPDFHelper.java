package org.verapdf.tools;

import org.apache.log4j.Logger;
import org.verapdf.as.ASAtom;
import org.verapdf.cos.COSObjType;
import org.verapdf.cos.COSObject;
import org.verapdf.pd.PDStructElem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Maksim Bezrukov
 */
public class TaggedPDFHelper {

	private static final Logger LOGGER = Logger.getLogger(TaggedPDFHelper.class);

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
			LOGGER.debug("Parent element for struct elements is null or not a COSDictionary");
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
		if (children.size() > 0) {
			List<PDStructElem> list = new ArrayList<>();
			for (int i = 0; i < children.size(); ++i) {
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
