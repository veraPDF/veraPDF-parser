package org.verapdf.pd;

import org.verapdf.cos.COSObject;

/**
 * @author Timur Kamalov
 */
public class PDPageTreeNode extends PDObject {

	private PDPageTreeBranch parent;

	public PDPageTreeNode() {
		this.parent = new PDPageTreeBranch();
	}

	public PDPageTreeNode(final COSObject obj) {
		super();
		parent = new PDPageTreeBranch();
		setObject(obj);
	}
}
