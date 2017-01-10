package org.verapdf.as.filters;

import org.verapdf.as.io.ASInputStream;

import java.io.IOException;

/**
 * @author Timur Kamalov
 */
public abstract class ASInFilter extends ASInputStream {

	private ASInputStream storedInStream;

	/**
	 * Constructor from encoded stream.
	 * @param inputStream is stream with initial encoded data.
	 * @throws IOException
     */
	protected ASInFilter(ASInputStream inputStream) throws IOException {
		this.storedInStream = inputStream;
	}

	protected ASInFilter(final ASInFilter filter) {
		if (filter != null) {
			this.storedInStream = filter;
		}
	}

	@Override
	public int read() throws IOException {
		byte[] temp = new byte[1];
		if (this.read(temp, 1) != -1) {
			return temp[0] & 0xFF;
		}
		return -1;
	}

	public int read(byte[] buffer, int size) throws IOException {
		return this.storedInStream != null ? this.storedInStream.read(buffer, size) : -1;
	}

	public int read(byte[] buffer) throws IOException {
		return this.read(buffer, buffer.length);
	}

	public int skip(int size) throws IOException {
		return this.storedInStream != null ? this.storedInStream.skip(size) : 0;
	}

	public void closeResource() throws IOException {
		this.storedInStream.closeResource();
	}

	public void reset() throws IOException {
		if (this.storedInStream != null) {
			this.storedInStream.reset();
		}
	}

	@Override
	public void close() throws IOException {
		if (this.storedInStream != null) {
			this.storedInStream.close();
		}
	}

	protected ASInputStream getInputStream() {
		return this.storedInStream;
	}

	protected void setInputStream(ASInputStream inputStream) {
		this.storedInStream = inputStream;
	}
}
