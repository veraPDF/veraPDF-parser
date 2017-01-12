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
package org.verapdf.pd;

import org.verapdf.cos.COSObject;

import java.io.IOException;

/**
 * @author Timur Kamalov
 */
public class PDPageTree {

	private PDPageTreeBranch root;

	public PDPageTree() {
		this.root = new PDPageTreeBranch();
	}

	public PDPageTree(final COSObject object) throws Exception {
		this.root = new PDPageTreeBranch(object);
	}

	public PDPageTreeBranch getRoot() {
		return root;
	}

	public COSObject getObject() {
		return this.getRoot().getObject();
	}

	public void setObject(final COSObject object) throws IOException {
		this.getRoot().setObject(object);
	}

	public boolean empty() {
		return this.getRoot().empty();
	}

	public int getPageCount() {
		return this.getRoot().getLeafCount();
	}

	public PDPage getPage(final int index) {
		if (index < this.getPageCount()) {
			final int totalIndex = index;
			final PDPage page = this.getRoot().findTerminalPDPage(index);
			if (page != null) {
				page.pageNumber = index;
				page.pagesTotal = totalIndex;
			}
			return page;
		} else {
			return null;
		}
	}

	public PDPage newPage(final int insertAt) throws IOException {
		final PDPage page = new PDPage(null);
		if (this.addPage(page, insertAt)) {
			page.pageNumber = (insertAt == -1 ? this.getPageCount() - 1 : insertAt);
		}
		return page;
	}

	public boolean addPage(final PDPage page, final int insertAt) throws IOException {
		final PDPageTreeBranch branch = this.getRoot().findTerminal(insertAt);

		if (branch.insertLeaf(page, insertAt)) {
			this.root = this.getRoot().getParent();
			return true;
		}
		return false;
	}

}
