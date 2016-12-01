package org.verapdf.as.filters;

import org.verapdf.as.io.ASOutputStream;

import java.io.IOException;

/**
 * @author Timur Kamalov
 */
public abstract class ASOutFilter implements ASOutputStream {

	private ASOutputStream storedOutputStream;

	protected ASOutFilter(final ASOutputStream outStream) {
		this.storedOutputStream = outStream;
	}

	protected ASOutputStream getStoredOutputStream() {
		return this.storedOutputStream;
	}

	private ASOutFilter(final ASOutFilter filter) {
		close();
	}

	public long write(final byte[] buffer) throws IOException {
		return this.storedOutputStream != null ?
				this.storedOutputStream.write(buffer) : 0;
	}

	public long write(final byte[] buffer, int offset, int size) throws IOException {
		return this.storedOutputStream != null ?
				this.storedOutputStream.write(buffer, offset, size) : 0;
 	}

	public void close() {
		this.storedOutputStream = null;
	}

}
