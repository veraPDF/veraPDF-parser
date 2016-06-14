package org.verapdf.pd;

import org.verapdf.cos.COSObject;
import org.verapdf.cos.COSStream;

/**
 * @author Timur Kamalov
 */
public class PDPageContentStream extends PDObject implements PDContentStream {

	private COSObject stream;

	public PDPageContentStream(COSObject stream) {
		super(stream);
	}

	@Override
	public COSStream getContents() {
		return (COSStream) this.stream.get();
	}

}
