package org.verapdf.as.io;

import java.io.IOException;

/**
 * @author Timur Kamalov
 */
public interface ASOutputStream {

	long write(final byte[] buffer) throws IOException; // TODO: should we add method write(byte [], int)?

	void close() throws IOException;

	void flush();
}
