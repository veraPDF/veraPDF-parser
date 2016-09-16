package org.verapdf.pd;

import org.verapdf.as.ASAtom;
import org.verapdf.cos.COSArray;
import org.verapdf.cos.COSObjType;
import org.verapdf.cos.COSObject;
import org.verapdf.pd.optionalcontent.PDOptionalContentProperties;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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

	public List<PDOutputIntent> getOutputIntents() {
		COSObject base = getKey(ASAtom.OUTPUT_INTENTS);
		if (base != null && base.getType() == COSObjType.COS_ARRAY) {
			if (base.isIndirect()) {
				base = base.getDirect();
			}
			List<PDOutputIntent> result = new ArrayList<>(base.size());
			for (COSObject obj : (COSArray) base.get()) {
				if (obj != null && obj.getType().isDictionaryBased()) {
					result.add(new PDOutputIntent(obj));
				}
			}
			return Collections.unmodifiableList(result);
		}
		return Collections.emptyList();
	}

	public PDOptionalContentProperties getOCProperties() {
		COSObject object = getKey(ASAtom.OCPROPERTIES);
		if (!object.empty() && object.getType() == COSObjType.COS_DICT) {
			return new PDOptionalContentProperties(object);
		}
		return null;
	}

	public PDOutlineDictionary getOutlines() {
		COSObject outlines = getKey(ASAtom.OUTLINES);
		if (outlines != null && outlines.getType().isDictionaryBased()) {
			return new PDOutlineDictionary(outlines);
		}
		return null;
	}

}
