package org.verapdf.as.io;

import org.verapdf.tools.IntReference;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author Timur Kamalov
 */
public abstract class ASInputStream extends InputStream {

	protected int nPos = -1;

	protected IntReference resourceUsers = new IntReference(1);

	public abstract int read() throws IOException;

	public abstract int read(byte[] buffer, int size) throws IOException;

	public abstract int skip(int size) throws IOException;

	public void close() throws IOException {
		this.resourceUsers.decrement();
		if(this.resourceUsers.equals(0)) {
			closeResource();
		}
	}

	public abstract void reset() throws IOException;

	public abstract void closeResource() throws IOException;

	public static ASInputStream createStreamFromStream(ASInputStream stream) {
		stream.resourceUsers.increment();
		return new ASInputStreamWrapper(stream);
	}
}
