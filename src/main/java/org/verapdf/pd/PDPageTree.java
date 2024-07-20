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
package org.verapdf.pd;

import org.verapdf.cos.COSObject;

/**
 * @author Timur Kamalov
 */
public class PDPageTree {

	private PDPageTreeBranch root;

	public PDPageTree() {
		this.root = new PDPageTreeBranch();
	}

	public PDPageTree(final COSObject object) {
		this.root = new PDPageTreeBranch(object);
	}

	public PDPageTreeBranch getRoot() {
		return root;
	}

	public COSObject getObject() {
		return this.root.getObject();
	}

	public void setObject(final COSObject object) {
		this.root.setObject(object);
	}

	public boolean empty() {
		return this.root.empty();
	}

	public int getPageCount() {
		return this.root.getLeafCount();
	}

	public PDPage getPage(final int index) {
		try {
			if (index < this.getPageCount()) {
				final PDPage page = this.root.findTerminalPDPage(index);
				if (page != null) {
					page.pageNumber = index;
					page.pagesTotal = index;
				}
				return page;
			} else {
				return null;
			}
		} catch (IndexOutOfBoundsException e) {
			throw new IndexOutOfBoundsException("Invalid page tree");
		}
	}

	public PDPage newPage(final int insertAt) {
		final PDPage page = new PDPage(null);
		if (this.addPage(page, insertAt)) {
			page.pageNumber = (insertAt == -1 ? this.getPageCount() - 1 : insertAt);
		}
		return page;
	}

	public boolean addPage(final PDPage page, final int insertAt) {
		final PDPageTreeBranch branch = this.root.findTerminal(insertAt);

		if (branch.insertLeaf(page, insertAt)) {
			this.root = this.root.getParent();
			return true;
		}
		return false;
	}

}
