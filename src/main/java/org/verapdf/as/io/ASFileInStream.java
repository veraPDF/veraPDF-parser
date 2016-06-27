package org.verapdf.as.io;


import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * @author Timur Kamalov
 */
public class ASFileInStream implements ASInputStream {

	private RandomAccessFile stream;
	private long offset;
	private long size;
	private long curPos;

	public ASFileInStream(RandomAccessFile stream, final long offset, final long size) {
		this.stream = stream;
		this.offset = offset;
		this.size = size;
		this.curPos = 0;
	}

	public int read(byte[] buffer, int sizeToRead) throws IOException {
		if (sizeToRead == 0 || this.size != nPos && this.size <= this.curPos) {
			return -1;
		}

		if (this.size != nPos && sizeToRead > this.size - this.curPos) {
			sizeToRead = (int) (this.size - this.curPos);
		}

		long prev = this.stream.getFilePointer();

		this.stream.seek(this.offset + this.curPos);
		int count = this.stream.read(buffer, 0, sizeToRead);

		this.stream.seek(prev);

		this.curPos += count;

		return count;
	}

	public int skip(int size) throws IOException {
		if (size == 0 || this.size != nPos && this.size <= this.curPos) {
			return 0;
		}

		if (this.size != nPos && size > this.size - this.curPos) {
			size = (int) (this.size - this.curPos);
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
		return false;
	}

}
