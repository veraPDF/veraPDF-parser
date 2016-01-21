package org.verapdf.io;

import org.verapdf.as.io.ASOutputStream;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * @author Timur Kamalov
 */
public class InternalOutputStream implements ASOutputStream {

	private final static String READ_WRITE_MODE = "rw";

	private RandomAccessFile os;

	public InternalOutputStream(final String fileName) throws FileNotFoundException {
		this.os = new RandomAccessFile(fileName, READ_WRITE_MODE);
	}

	public long write(final byte[] buffer) throws IOException {
		long oldPos = this.os.getFilePointer();
		this.os.write(buffer);
		return tellp() - oldPos;
	}

	public void close() throws IOException {
		this.os.close();
	}

	public void flush() throws IOException {
		//TODO : unnecessary operation for RandomAccessFile
	}

	public long tellp() throws IOException {
		return this.os.getFilePointer();
	}

	public InternalOutputStream seekp(long offset) throws IOException {
		this.os.seek(offset);
		return this;
	}

	public InternalOutputStream write(final char value) throws IOException {
		this.os.writeChar(value);
		return this;
	}

	public InternalOutputStream write(final byte value) throws IOException {
		this.os.writeByte(value);
		return this;
	}

	public InternalOutputStream write(final boolean value) throws IOException {
		this.os.writeBoolean(value);
		return this;
	}

	public InternalOutputStream write(final long value) throws IOException {
		this.os.writeLong(value);
		return this;
	}

	public InternalOutputStream write(final double value) throws IOException {
		this.os.writeDouble(value);
		return this;
	}

	public InternalOutputStream write(final String value) throws IOException {
		this.os.writeBytes(value);
		return this;
	}

}
