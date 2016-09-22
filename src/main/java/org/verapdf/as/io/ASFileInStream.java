package org.verapdf.as.io;


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * @author Timur Kamalov
 */
public class ASFileInStream extends ASInputStream {

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

	@Override
	public int read() throws IOException {
		if (this.curPos < this.size) {
			long prev = stream.getFilePointer();

			stream.seek(this.offset + this.curPos);
			int result = this.stream.readByte() & 0xFF;
			curPos++;

			this.stream.seek(prev);

			return result;
		} else {
			return -1;
		}
	}

	@Override
	public int read(byte[] buffer, int sizeToRead) throws IOException {
		if (sizeToRead == 0 || this.size != nPos && this.curPos >= this.size) {
			return -1;
		}

		if (this.size != nPos && sizeToRead > this.size - this.curPos) {
			sizeToRead = (int) (this.size - this.curPos);
		}

		long prev = this.stream.getFilePointer();

		this.stream.seek(this.offset + this.curPos);

		try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {
			byte[] temp = new byte[1024];
			int n;

			while (sizeToRead > 0 && (n = this.stream.read(temp, 0, Math.min(temp.length, sizeToRead))) != -1) {
				output.write(temp, 0, n);
				sizeToRead -= n;
			}

			byte[] byteArray = output.toByteArray();
			int count = byteArray.length;
			System.arraycopy(byteArray, 0, buffer, 0, count);

			this.stream.seek(prev);
			this.curPos += count;
			return count;
		}
	}

	@Override
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

	@Override
	public void close() {
	}

	@Override
	public void reset() {
		this.curPos = 0;
	}

}
