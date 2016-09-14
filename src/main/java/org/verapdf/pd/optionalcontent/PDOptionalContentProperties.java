package org.verapdf.pd.optionalcontent;

import org.verapdf.as.ASAtom;
import org.verapdf.cos.COSArray;
import org.verapdf.cos.COSDictionary;
import org.verapdf.cos.COSObjType;
import org.verapdf.cos.COSObject;
import org.verapdf.pd.PDObject;

/**
 * @author Timur Kamalov
 */
public class PDOptionalContentProperties extends PDObject {

	public PDOptionalContentProperties(COSObject obj) {
		super(obj);
	}

	public String[] getGroupNames() {
		COSObject ocgs = getObject().getKey(ASAtom.OCGS);
		if (!ocgs.empty() && ocgs.getType() == COSObjType.COS_ARRAY) {
			COSArray ocgsArray = (COSArray) ocgs.getDirectBase();
			int size = ocgsArray.size();
			String[] groups = new String[size];

			for(int i = 0; i < size; ++i) {
				COSObject obj = ocgs.at(i);
				if (!obj.empty() && obj.getType() == COSObjType.COS_DICT) {
					COSDictionary ocgDict = (COSDictionary) obj.getDirectBase();
					groups[i] = ocgDict.getStringKey(ASAtom.NAME);
				}
			}

			return groups;
		} else {
			return null;
		}
	}

}
