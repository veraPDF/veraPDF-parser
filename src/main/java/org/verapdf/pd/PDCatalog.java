package org.verapdf.pd;

import org.verapdf.as.ASAtom;
import org.verapdf.cos.COSObjType;
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

	public PDMetadata getMetadata() {
		COSObject metadata = getKey(ASAtom.METADATA);
		if (metadata != null && metadata.getType() == COSObjType.COS_STREAM) {
			return new PDMetadata(metadata);
		}
		return null;
	}

	public PDStructTreeRoot getStructTreeRoot() {
		COSObject base = getKey(ASAtom.STRUCT_TREE_ROOT);
		if (base != null && base.getType() == COSObjType.COS_DICT) {
			return new PDStructTreeRoot(base);
		}
		return null;
	}

}
