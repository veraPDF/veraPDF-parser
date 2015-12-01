package org.verapdf.cos;

/**
 * @author Timur Kamalov
 */
public class COSXRefInfo {

	private long startXRef;
	private COSXRefSection xref;
	private COSTrailer trailer;

	public COSXRefInfo() {
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

	public void setTrailer(COSTrailer trailer) {
		this.trailer = trailer;
	}

}
