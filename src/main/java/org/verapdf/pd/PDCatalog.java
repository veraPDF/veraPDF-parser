package org.verapdf.pd;

import org.verapdf.as.ASAtom;
import org.verapdf.cos.COSObject;

import java.io.IOException;

/**
 * @author Timur Kamalov
 */
public class PDCatalog extends PDObject {

	private PDPageTree pages;

	public PDCatalog() {
		super();
		this.pages = new PDPageTree();
	}

	public PDCatalog(final COSObject object) throws IOException {
		super(object);
		this.pages = new PDPageTree();
	}

	public PDPageTree getPageTree() throws IOException {
		if (pages.empty()) {
			final COSObject pages = super.getObject().getKey(ASAtom.PAGES);
			this.pages.setObject(pages);
		}
		return pages;
	}

}
