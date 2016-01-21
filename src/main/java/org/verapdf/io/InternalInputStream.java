package org.verapdf.io;

import org.verapdf.as.io.ASInputStream;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * @author Timur Kamalov
 */
public class InternalInputStream implements ASInputStream {

	private final static String READ_ONLY_MODE = "r";

	private RandomAccessFile source;

	public InternalInputStream(final String fileName) throws FileNotFoundException {
		this.source = new RandomAccessFile(fileName, READ_ONLY_MODE);
	}

	public int read(byte[] buffer, int size) throws IOException {
		return this.source.read(buffer, 0, size);
	}

	public int skip(int size) throws IOException {
		return this.source.skipBytes(size);
	}

	public void close() throws IOException {
		this.source.close();
	}

	public void reset() throws IOException {
		this.source.seek(0);
	}

	public boolean isCloneable() {
		return false;
	}

	public long tellg() throws IOException {
		return (int) this.source.getFilePointer();
	}

	public InternalInputStream seekg(final long pos) throws IOException {
		this.source.seek(pos);
		return this;
	}

	public InternalInputStream seekFromEnd(final long pos) throws IOException {
		final long size = this.source.length();
		this.source.seek(size - pos);
		return this;
	}

	public InternalInputStream seekFromCurPos(final long pos) throws IOException {
		this.source.seek(tellg() + pos);
		return this;
	}

	public InternalInputStream get(Character ch) throws IOException {
		ch = this.source.readChar();
		return this;
	}

	public char get() throws IOException {
		return (char) this.source.read();
	}

	public InternalInputStream unread() throws IOException{
		this.source.seek(this.source.getFilePointer() - 1);
		return this;
	}

	public boolean isEof() throws IOException {
		return this.source.getFilePointer() == this.source.length();
	}

}
