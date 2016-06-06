package org.verapdf.as.filters;

import org.verapdf.as.io.ASOutputStream;

import java.io.IOException;

/**
 * @author Timur Kamalov
 */
public class ASOutFilter implements ASOutputStream {

	private ASOutputStream storedOutputStream;

	protected ASOutFilter(final ASOutputStream outStream) {
		this.storedOutputStream = outStream;
	}

	private ASOutFilter(final ASOutFilter filter) {
		close();
	}

	public long write(final byte[] buffer) throws IOException {
		//TODO : size
		return this.storedOutputStream != null ? this.storedOutputStream.write(buffer) : 0;
	}



	public void flush() {
		this.storedOutputStream.flush();
	}

	public void close() {
		this.storedOutputStream = null;
	}

}
