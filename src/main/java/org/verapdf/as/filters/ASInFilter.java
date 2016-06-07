package org.verapdf.as.filters;

import org.verapdf.as.io.ASInputStream;

import java.io.IOException;

/**
 * @author Timur Kamalov
 */
public class ASInFilter implements ASInputStream {

	private ASInputStream storedInStream;

	protected ASInFilter(ASInputStream inputStream) {
		this.storedInStream = inputStream;
	}

	protected ASInFilter(final ASInFilter filter) {
		if (filter.storedInStream != null) {
			this.storedInStream = filter.storedInStream;
		}
	}

	public long read(byte[] buffer, long size) throws IOException {
		return this.storedInStream != null ? this.storedInStream.read(buffer, size) : 0;
	}

	public long skip(long size) throws IOException {
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

}
