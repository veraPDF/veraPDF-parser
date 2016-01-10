package org.verapdf.pd;

import org.verapdf.as.ASAtom;
import org.verapdf.cos.COSObject;

/**
 * @author Timur Kamalov
 */
public class PDCatalog extends PDObject {

	private PDPageTree pages;

	public PDCatalog() {
		super();
		this.pages = new PDPageTree();
	}

	public PDCatalog(final COSObject object) {
		super(object);
		this.pages = new PDPageTree();
	}

	public PDPageTree getPageTree() {
		if (this.pages.empty) {
			this.pages.setObject(getObject().getKey(ASAtom.PAGES));
		}
		return pages;
	}

}
