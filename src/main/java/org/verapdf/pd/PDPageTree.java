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

	public PDPageTree(final COSObject object) throws Exception {
		this.root = new PDPageTreeBranch(object);
	}

	public PDPageTreeBranch getRoot() {
		return root;
	}

	public COSObject getObject() {
		return this.getRoot().getObject();
	}

	public void setObject(final COSObject object) throws Exception {
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
			final PDPageTreeBranch term = this.getRoot().findTerminal(index);
			final PDPage page = (PDPage) term.getChild(index);
			if (page != null) {
				page.pageNumber = index;
				page.pagesTotal = totalIndex;
			}
			return page;
		} else {
			return null;
		}
	}

	public PDPage newPage(final int insertAt) throws Exception {
		final PDPage page = new PDPage(null);
		if (this.addPage(page, insertAt)) {
			page.pageNumber = (insertAt == -1 ? this.getPageCount() - 1 : insertAt);
		}
		return page;
	}

	public boolean addPage(final PDPage page, final int insertAt) throws Exception {
		final PDPageTreeBranch branch = this.getRoot().findTerminal(insertAt);

		if (branch.insertLeaf(page, insertAt)) {
			this.root = this.getRoot().getParent();
			return true;
		}
		return false;
	}

}
