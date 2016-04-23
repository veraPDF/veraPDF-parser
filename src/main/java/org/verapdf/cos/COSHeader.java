package org.verapdf.cos;

/**
 * @author Timur Kamalov
 */
public class COSHeader {

	private long headerOffset;

	private String header;

	private float version;

	private int headerCommentByte1;
	private int headerCommentByte2;
	private int headerCommentByte3;
	private int headerCommentByte4;

	public COSHeader() {
	}

	public COSHeader(final String header) {
		this.header = header;
	}

	public void setBinaryHeaderBytes(int first, int second, int third, int fourth) {
		this.headerCommentByte1 = first;
		this.headerCommentByte2 = second;
		this.headerCommentByte3 = third;
		this.headerCommentByte4 = fourth;
	}

	// GETTERS & SETTERS

	public long getHeaderOffset() {
		return headerOffset;
	}

	public void setHeaderOffset(long headerOffset) {
		this.headerOffset = headerOffset;
	}

	public String getHeader() {
		return header;
	}

	public void setHeader(final String header) {
		this.header = header;
	}

	public float getVersion() {
		return version;
	}

	public void setVersion(float version) {
		this.version = version;
	}

	public int getHeaderCommentByte1() {
		return headerCommentByte1;
	}

	public void setHeaderCommentByte1(final int headerCommentByte1) {
		this.headerCommentByte1 = headerCommentByte1;
	}

	public int getHeaderCommentByte2() {
		return headerCommentByte2;
	}

	public void setHeaderCommentByte2(final int headerCommentByte2) {
		this.headerCommentByte2 = headerCommentByte2;
	}

	public int getHeaderCommentByte3() {
		return headerCommentByte3;
	}

	public void setHeaderCommentByte3(final int headerCommentByte3) {
		this.headerCommentByte3 = headerCommentByte3;
	}

	public int getHeaderCommentByte4() {
		return headerCommentByte4;
	}

	public void setHeaderCommentByte4(final int headerCommentByte4) {
		this.headerCommentByte4 = headerCommentByte4;
	}

}
