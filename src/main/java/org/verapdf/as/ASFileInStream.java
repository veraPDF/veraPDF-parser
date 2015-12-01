package org.verapdf.as;


import java.io.InputStream;

/**
 * @author Timur Kamalov
 */
public class ASFileInStream implements ASInputStream {

	private InputStream stream;
	private int offset;
	private int size;
	private int curPos;

	public ASFileInStream(InputStream stream, final int offset, final int size) {
		this.stream = stream;
		this.offset = offset;
		this.size = size;
		this.curPos = 0;
	}

	public int read(byte[] buffer, int size) {
		if (size == 0 || this.size != nPos && this.size <= this.curPos) {
			return 0;
		}

		if (this.size != nPos && size > this.size - this.curPos) {
			size = this.size - this.curPos;
		}

		int prev = this.stream.getPosition;

		this.stream.seek(this.offset + this.curPos);

		this.stream.read(buffer, size);
		//TODO : deal with this count thing

		return count;
	}

	public int skip(int size) {
		if (size == 0 || this.size != nPos && this.size <= this.curPos) {
			return 0;
		}

		if (this.size != nPos && size > this.size - this.curPos) {
			size = this.size - this.curPos;
		}

		this.curPos += size;

		return size;
	}

	public void close() {

	}

	public void reset() {
		this.curPos = 0;
	}

	public boolean isCloneable() {
		return true;
	}

}
