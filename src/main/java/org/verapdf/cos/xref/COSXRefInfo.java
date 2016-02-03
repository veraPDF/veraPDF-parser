package org.verapdf.cos.xref;

import org.verapdf.cos.COSObject;
import org.verapdf.cos.COSTrailer;

/**
 * @author Timur Kamalov
 */
public class COSXRefInfo {

	private long startXRef;
	private COSXRefSection xref;
	private COSTrailer trailer;

	public COSXRefInfo() throws Exception {
		this.startXRef = 0;
		this.xref = new COSXRefSection();
		this.trailer = new COSTrailer();
	}

	public long getStartXRef() {
		return this.startXRef;
	}

	public void setStartXRef(final long startXRef) {
		this.startXRef = startXRef;
	}

	public COSXRefSection getXRefSection() {
		return this.xref;
	}

	public void setXref(COSXRefSection xref) {
		this.xref = xref;
	}

	public COSTrailer getTrailer() {
		return this.trailer;
	}

	public void setTrailer(final COSObject object) throws Exception {
		this.trailer.setObject(object);
	}

}
