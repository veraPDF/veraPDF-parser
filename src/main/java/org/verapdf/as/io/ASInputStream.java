package org.verapdf.as.io;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author Timur Kamalov
 */
public abstract class ASInputStream extends InputStream {

	int nPos = -1;

	public abstract int read() throws IOException;

	public abstract int read(byte[] buffer, int size) throws IOException;

	public abstract int skip(int size) throws IOException;

	public abstract void close() throws IOException;

	public abstract void reset() throws IOException;

}
