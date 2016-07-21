package org.verapdf.pd;

import org.verapdf.as.ASAtom;
import org.verapdf.as.exceptions.StringExceptions;
import org.verapdf.cos.*;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Timur Kamalov
 */
public class PDPageTreeBranch extends PDPageTreeNode {

	private final int PD_TREE_MAX_CHILD = 11;

	private int leafCount;
	private boolean isTerminal;
	private List<PDPageTreeNode> children;

	public PDPageTreeBranch() {
		this.isTerminal = true;
		this.leafCount = 0;
		this.children = new ArrayList<PDPageTreeNode>();
	}

	public PDPageTreeBranch(final COSObject obj) {
		this.isTerminal = true;
		this.leafCount = 0;
		this.children = new ArrayList<PDPageTreeNode>();

		super.setObject(obj);
	}

	private PDPageTreeBranch(final PDPageTreeBranch leftChild, final PDPageTreeBranch rightChild) {
		this.isTerminal = false;
		this.children = new ArrayList<PDPageTreeNode>();

		initialize();
		this.children.add(leftChild);
		this.children.add(rightChild);

		this.leafCount = leftChild.leafCount + rightChild.leafCount;

		updateToObject();
		leftChild.setParent(this);
		rightChild.setParent(this);
	}

	public int getLeafCount() {
		return leafCount;
	}

	public int getChildCount() {
		return children.size();
	}

	public PDPageTreeNode getChild(final int index) {
		return children.get(index);
	}

	public int getIndex(final PDPageTreeNode node) {
		return children.indexOf(node);
	}

	public PDPageTreeBranch findTerminal(int index) {
		if (isTerminal) {
			index = Math.min(index, (int) getLeafCount());
			return this;
		}

		for (PDPageTreeNode branch : this.children) {
			if (index >= branch.getLeafCount()) {
				index -= branch.getLeafCount();
			} else {
				return branch.findTerminal(index);
			}
		}

		int lastIndex = this.children.size() - 1;
		return this.children.get(lastIndex).findTerminal(index);
	}

	public boolean insertLeaf(final PDPage leaf, int insertAt) throws Exception {
		insertAt = Math.min(insertAt, getChildCount());
		incLeafCount();
		return insertNode(leaf, insertAt);
	}

	protected void updateFromObject() {
		clear();

		COSObject kids = getObject().getKey(ASAtom.KIDS);
		for (int i = 0; i < kids.size(); i++) {
			COSObject obj = kids.at(i);

			PDPageTreeNode kid_i;

			if (obj.getNameKey(ASAtom.TYPE).equals(ASAtom.PAGE)) {
				kid_i = new PDPage(obj);
			} else if (obj.getNameKey(ASAtom.TYPE).equals(ASAtom.PAGES)) {
				kid_i = new PDPageTreeBranch(obj);
				isTerminal = false;
			} else {
				//TODO : ASException
				throw new RuntimeException("PDPageTreeBranch::UpdateFromObject()" + StringExceptions.UNKNOWN_TYPE_PAGE_TREE_NODE);
			}

			kid_i.setParent(this);

			this.children.add(kid_i);
		}

		this.leafCount = getObject().getIntegerKey(ASAtom.COUNT).intValue();
	}

	protected void updateToObject() {
		COSObject branch = getObject();
		COSObject kids = COSArray.construct();

		for (PDPageTreeNode node : this.children) {
			kids.add(node.getObject());
		}
		branch.setKey(ASAtom.KIDS, kids);

		COSObject count = COSInteger.construct(this.leafCount);
		branch.setKey(ASAtom.COUNT, count);
		PDPageTreeBranch parentNode = getParent();
		if (parentNode != null) {
			branch.setKey(ASAtom.PARENT, parentNode.getObject());
		}
	}

	private void initialize() {
		COSObject dict = COSDictionary.construct();
		dict.setNameKey(ASAtom.TYPE, ASAtom.PAGES);
		dict.setArrayKey(ASAtom.KIDS);
		dict.setIntegerKey(ASAtom.COUNT, 0);
		COSObject branch = COSIndirect.construct(dict);
		setObject(branch);
	}

	private boolean insertNode(final PDPageTreeNode node, int insertAt) {
		node.setParent(this);
		this.children.add(insertAt, node);
		getObject().getKey(ASAtom.KIDS).insert(insertAt, node.getObject());

		if (getChildCount() > PD_TREE_MAX_CHILD) {
			PDPageTreeBranch rightNeighbour = new PDPageTreeBranch(this.getObject());

			if (getParent() != null) {
				// Determining the position of current branch in its parent's childlist
				int insPos = getParent().getIndex(this) + 1;
				return getParent().insertNode(rightNeighbour, insPos);
			} else {
				new PDPageTreeBranch(this, rightNeighbour);
				return true;
			}
		}

		return false;
	}

	private void incLeafCount() {
		PDPageTreeBranch branch = this;
		while (branch != null) {
			++branch.leafCount;
			branch.getObject().setIntegerKey(ASAtom.COUNT, branch.leafCount);

			branch = branch.getParent();
		}
	}

	@Override
	public void clear() {
		this.children.clear();

		this.leafCount = 0;
		this.isTerminal = true;
	}

}
