package org.verapdf.io;

import org.verapdf.as.io.ASOutputStream;

import java.io.File;
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
		//check if file already exists and delete it
		File file = new File(fileName);
		if (file.exists()) {
			if (!file.delete()) {
				throw new RuntimeException("Cannot create file : " + fileName);
			}
		}
		this.os = new RandomAccessFile(file, READ_WRITE_MODE);
	}

	public long write(final byte[] buffer) throws IOException {
		long oldPos = this.os.getFilePointer();
		this.os.write(buffer);
		return getOffset() - oldPos;
	}

	public long write(final byte[] buffer, final int size) throws IOException {
		long oldPos = this.os.getFilePointer();
		this.os.write(buffer, 0, size);
		return getOffset() - oldPos;
	}

	public void close() throws IOException {
		this.os.close();
	}

	public void seekEnd() throws IOException {
		this.os.seek(this.os.length());
	}

	public long getOffset() throws IOException {
		return this.os.getFilePointer();
	}

	public InternalOutputStream seek(long offset) throws IOException {
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

	public InternalOutputStream write(final int value) throws IOException {
		this.os.writeInt(value);
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
