/**
 * This file is part of veraPDF Parser, a module of the veraPDF project.
 * Copyright (c) 2015-2025, veraPDF Consortium <info@verapdf.org>
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
package org.verapdf.pd;

import org.verapdf.as.ASAtom;
import org.verapdf.cos.COSObject;

/**
 * @author Timur Kamalov
 */
public class PDPageTreeNode extends PDObject {

	private PDPageTreeBranch parent;

	public PDPageTreeNode() {
	}

	public PDPageTreeNode(final COSObject obj) {
		super.setObject(obj);
	}

	public PDPageTreeBranch getParent() {
		return this.parent;
	}

	public void setParent(final PDPageTreeBranch parent) {
		this.parent = parent;
		if (parent != null) {
			super.getObject().setKey(ASAtom.PARENT, parent.getObject());
		}
	}

	public int getLeafCount() {
		return 1;
	}

	public PDPageTreeBranch findTerminal(int index) {
		if (parent == null) {
			return null;
		}

		return parent;
	}

	public PDPage findTerminalPDPage(int index) {
		if (parent == null) {
			return null;
		}

		index += this.parent.getIndex(this);
		return (PDPage) parent.getChild(index);
	}

	protected COSObject getInheritableResources() {
		COSObject value = getObject().getKey(ASAtom.RESOURCES);
		if (value != null && !value.empty()) {
			return value;
		}

		if (parent != null)	{
			return parent.getInheritableResources();
		}

		return null;
	}

}
