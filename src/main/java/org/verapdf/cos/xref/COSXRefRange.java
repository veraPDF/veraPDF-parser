package org.verapdf.cos.xref;

/**
 * @author Timur Kamalov
 */
public class COSXRefRange {

	public int start;
	public int count;

	public COSXRefRange() {
	}

	public COSXRefRange(final int start) {
		this(start, 1);
	}

	public COSXRefRange(final int start, final int count) {
		this.start = start;
		this.count = count;
	}

	public int next() {
		return this.start + this.count;
	}

}
