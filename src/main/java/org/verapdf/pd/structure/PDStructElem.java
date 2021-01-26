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
import org.verapdf.tools.TaggedPDFHelper;

import java.util.List;
import java.util.Map;

/**
 * @author Maksim Bezrukov
 */
public class PDStructElem extends PDStructTreeNode {

	private Map<ASAtom, ASAtom> rootRoleMap;

	public PDStructElem(COSObject obj, Map<ASAtom, ASAtom> rootRoleMap) {
		super(obj);
		this.rootRoleMap = rootRoleMap;
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

	public String getActualText() {
		return getStringKey(ASAtom.ACTUAL_TEXT);
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
			return new PDStructElem(parentObject, this.rootRoleMap);
		}
		return null;
	}

	public StructureType getDefaultStructureType() {
		return TaggedPDFHelper.getDefaultStructureType(this.getStructureType(), this.rootRoleMap);
	}

	@Override
	public List<PDStructElem> getStructChildren() {
		return TaggedPDFHelper.getStructElemStructChildren(getObject(), rootRoleMap);
	}

	@Override
	public List<Object> getChildren() {
		return TaggedPDFHelper.getStructElemChildren(getObject(), rootRoleMap);
	}
}
