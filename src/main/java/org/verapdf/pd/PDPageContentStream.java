package org.verapdf.pd;

import org.verapdf.cos.COSObject;

/**
 * @author Timur Kamalov
 */
public class PDPageContentStream extends PDContentStream {

	public PDPageContentStream(COSObject contents) {
		super(contents);
	}

	@Override
	public COSObject getContents() {
		return super.getObject();
	}

	@Override
	public void setContents(final COSObject contents) {
		super.setObject(contents);
	}

}
