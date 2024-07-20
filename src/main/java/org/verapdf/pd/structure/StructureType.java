/**
 * This file is part of veraPDF Parser, a module of the veraPDF project.
 * Copyright (c) 2015-2024, veraPDF Consortium <info@verapdf.org>
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
import org.verapdf.cos.COSObjType;
import org.verapdf.cos.COSObject;
import org.verapdf.tools.TaggedPDFConstants;

/**
 * @author Maksim Bezrukov
 */
public class StructureType {
	private final ASAtom type;
	private final PDStructureNameSpace nameSpace;

	private StructureType(ASAtom type, PDStructureNameSpace nameSpace) {
		this.type = type;
		this.nameSpace = nameSpace;
	}

	public static StructureType createStructureType(COSObject object) {
		if (object == null) {
			throw new IllegalArgumentException("Argument object can not be null");
		}
		COSObjType objType = object.getType();
		if (objType == COSObjType.COS_NAME) {
			return createStructureType(object, null);
		}
		if (objType == COSObjType.COS_ARRAY && object.size() >= 2) {
			return createStructureType(object.at(0), object.at(1));
		}
		return null;
	}

	public static StructureType createStructureType(COSObject type, COSObject ns) {
		if (type != null && type.getType() == COSObjType.COS_NAME) {
			if (ns != null && ns.getType() == COSObjType.COS_DICT) {
				return new StructureType(type.getName(), PDStructureNameSpace.createNameSpace(ns));
			} else {
				return new StructureType(type.getName(), null);
			}
		}
		return null;
	}

	public static StructureType createStructureType(ASAtom type, PDStructureNameSpace nameSpace) {
		if (type != null) {
			return new StructureType(type, nameSpace);
		}
		return null;
	}

	public static StructureType createStructureType(ASAtom type) {
		if (type != null) {
			return new StructureType(type, null);
		}
		return null;
	}

	public ASAtom getType() {
		return type;
	}

	public String getNameSpaceURI() {
		return this.nameSpace == null ? TaggedPDFConstants.PDF_NAMESPACE : this.nameSpace.getNS();
	}

	public PDStructureNameSpace getNameSpace() {
		return nameSpace;
	}
}
