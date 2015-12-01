package org.verapdf.io;

import org.verapdf.as.ASInputStream;

import java.io.InputStream;

/**
 * @author Timur Kamalov
 */
public class InternalInputStream implements ASInputStream {

	private InputStream stream;

	public InternalInputStream(final String fileName) {
		this.stream = new OurCoolStream(fileName);
	}

	public int read(byte[] buffer, int size) {
		this.stream.read(buffer, size);
		//TODO : count stuff again
		return this.stream.count;
	}

	public int skip(int size) {
		this.stream.skip(size);
		return this.stream.count;
	}

	public void close() {
		this.stream.close();
	}

	public void reset() {
		this.stream.seek(0);
	}

	public boolean isCloneable() {
		return false;
	}

}
