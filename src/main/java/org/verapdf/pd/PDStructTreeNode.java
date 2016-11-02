package org.verapdf.pd;

import org.verapdf.cos.COSObject;

import java.util.List;

/**
 * @author Maksim Bezrukov
 */
public abstract class PDStructTreeNode extends PDObject {

	protected PDStructTreeNode(COSObject obj) {
		super(obj);
	}

	public abstract List<PDStructElem> getChildren();
}
