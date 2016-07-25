package org.verapdf.pd;

import org.verapdf.cos.COSObject;

/**
 * @author Timur Kamalov
 */
public abstract class PDContentStream extends PDObject {

	protected PDContentStream(COSObject contents) {
		super(contents);
	}

	public abstract COSObject getContents();

	public abstract void setContents(final COSObject contents);

}
