package org.verapdf.as;

/**
 * @author Timur Kamalov
 */
public interface ASInputStream {

	int nPos = -1;

	int read(byte[] buffer, int size);

	int skip(int size);

	void close();

	void reset();

	boolean isCloneable();

	//TODO : clone method

}
