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

		index += this.parent.getIndex(this);
		return parent;
	}

	public PDPage findTerminalPDPage(int index) {
		if(parent == null) {
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
