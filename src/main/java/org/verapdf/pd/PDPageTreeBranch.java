package org.verapdf.pd;

import org.verapdf.cos.COSObject;

import java.util.List;

/**
 * @author Timur Kamalov
 */
public class PDPageTreeBranch extends PDPageTreeNode {

	private long leafCount;
	private boolean isTerminal;
	private List<PDPageTreeNode> children;

	public PDPageTreeBranch(long leafCount) {
		this.isTerminal
		this.leafCount = 0;

	}

	public PDPageTreeBranch(COSObject obj, long leafCount) {
		super(obj);
		this.leafCount = leafCount;
	}
}
