package org.verapdf.pd;

import org.verapdf.as.ASAtom;
import org.verapdf.cos.COSObjType;
import org.verapdf.cos.COSObject;

/**
 * @author Maksim Bezrukov
 */
public class PDNamesDictionary extends PDObject {

	public PDNamesDictionary(COSObject obj) {
		super(obj);
	}

	public PDNameTreeNode getEmbeddedFiles() {
		return getNameTreeByName(ASAtom.EMBEDDED_FILES);
	}

	public PDNameTreeNode getJavaScript() {
		return getNameTreeByName(ASAtom.JAVA_SCRIPT);
	}

	private PDNameTreeNode getNameTreeByName(ASAtom name) {
		COSObject base = getKey(name);
		if (base != null && base.getType() == COSObjType.COS_DICT) {
			return PDNameTreeNode.create(base);
		}
		return null;
	}
}
