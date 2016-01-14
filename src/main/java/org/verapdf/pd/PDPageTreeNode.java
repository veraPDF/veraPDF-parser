package org.verapdf.pd;

import org.verapdf.as.ASAtom;
import org.verapdf.cos.COSObject;

import java.io.IOException;

/**
 * @author Timur Kamalov
 */
public class PDPageTreeNode extends PDObject {

	private PDPageTreeBranch parent;

	public PDPageTreeNode() {
	}

	public PDPageTreeNode(final COSObject obj) throws Exception {
		setObject(obj);
	}

	public PDPageTreeBranch getParent() {
		return this.parent;
	}

	public void setParent(final PDPageTreeBranch parent) throws IOException {
		this.parent = parent;
		if (parent != null) {
			getObject().setKey(ASAtom.PARENT, parent.getObject());
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

}
