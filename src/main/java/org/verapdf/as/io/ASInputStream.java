package org.verapdf.as.io;

import java.io.IOException;

/**
 * @author Timur Kamalov
 */
public interface ASInputStream {

	long nPos = -1;

	long read(byte[] buffer, long size) throws IOException;

	long skip(long size) throws IOException;

	void close() throws IOException;

	void reset() throws IOException;

}
