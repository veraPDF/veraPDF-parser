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
		return getRoot().getObject();
	}

	public void setObject(final COSObject object) throws Exception {
		getRoot().setObject(object);
	}

	public boolean empty() {
		return getRoot().empty();
	}

	public int getPageCount() {
		return getRoot().getLeafCount();
	}

	public PDPage getPage(int index) {
		if (index < getPageCount()) {
			int totalIndex = index;
			PDPageTreeBranch term = getRoot().findTerminal(index);
			PDPage page = (PDPage) term.getChild(index);
			if (page != null) {
				page.num = index;
				page.totalNum = totalIndex;
			}
			return page;
		} else {
			return null;
		}
	}

	public PDPage newPage(int insertAt) throws Exception {
		PDPage page = new PDPage(null);
		if (addPage(page, insertAt)) {
			page.num = insertAt == -1 ? getPageCount() - 1 : insertAt;
		}
		return page;
	}

	public boolean addPage(PDPage page, int insertAt) throws Exception {
		PDPageTreeBranch branch = getRoot().findTerminal(insertAt);

		if (branch.insertLeaf(page, insertAt)) {
			this.root = getRoot().getParent();
			return true;
		}
		return false;
	}

}
