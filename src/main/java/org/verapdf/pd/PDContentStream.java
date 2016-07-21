package org.verapdf.pd;

import org.verapdf.cos.COSObject;

/**
 * @author Timur Kamalov
 */
public interface PDContentStream {

	COSObject getContents();

	void setContents(final COSObject contents);

}
