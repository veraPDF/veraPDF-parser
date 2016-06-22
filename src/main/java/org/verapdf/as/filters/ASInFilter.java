package org.verapdf.as.filters;

import org.verapdf.as.io.ASInputStream;

import java.io.IOException;

/**
 * @author Timur Kamalov
 */
public abstract class ASInFilter implements ASInputStream {

	private ASInputStream storedInStream;

	/**
	 * Constructor. It decodes data and uses decoded stream as internal input
	 * stream.
	 * @param inputStream is stream with initial encoded data.
	 * @throws IOException
     */
	protected ASInFilter(ASInputStream inputStream) throws IOException {
		this.storedInStream = inputStream;
	}

	protected ASInFilter(final ASInFilter filter) {
		if (filter.storedInStream != null) {
			this.storedInStream = filter.storedInStream;
		}
	}

	public int read(byte[] buffer, int size) throws IOException {
		return this.storedInStream != null ? this.storedInStream.read(buffer, size) : -1;
	}

	public int skip(int size) throws IOException {
		return this.storedInStream != null ? this.storedInStream.skip(size) : 0;
	}

	public void close() throws IOException {
		this.storedInStream = null;
	}

	public void reset() throws IOException {
		if (this.storedInStream != null) {
			this.storedInStream.reset();
		}
	}

	protected ASInputStream getInputStream() {
		return this.storedInStream;
	}

	protected void setInputStream(ASInputStream inputStream) {
		this.storedInStream = inputStream;
	}

	protected abstract void decode() throws IOException;	//TODO: I think we should remove this

}
