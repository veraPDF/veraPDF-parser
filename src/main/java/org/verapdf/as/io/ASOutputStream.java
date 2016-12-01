package org.verapdf.as.io;

import java.io.IOException;

/**
 * @author Timur Kamalov
 */
public interface ASOutputStream {

	long write(final byte[] buffer) throws IOException;

	long write(final byte[] buffer, int offset, int size) throws IOException;

	void close() throws IOException;
}
