package org.verapdf.cos;

/**
 * @author Timur Kamalov
 */
public class COSHeader {

	private String header;

	public COSHeader() {
	}

	public COSHeader(final String header) {
		this.header = header;
	}

	public String get() {
		return header;
	}

	public void set(String header) {
		this.header = header;
	}

}
