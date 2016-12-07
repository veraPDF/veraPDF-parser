package org.verapdf.io;

import org.verapdf.as.filters.io.ASBufferingInFilter;
import org.verapdf.as.io.ASInputStream;
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
	private File file;
	/**
	 * Creates temp file and opens output stream into it. File path can be
	 * obtained after.
	 *
	 * @return new InternalOutputStream.
	 * @throws IOException
	 */
	public static InternalOutputStream getInternalOutputStream() throws IOException {
		File tempFile = File.createTempFile("tmp_pdf_file", ".pdf");
		tempFile.deleteOnExit();
		return new InternalOutputStream(tempFile);
	}

	public InternalOutputStream(final String fileName) throws FileNotFoundException {
		this(getFileFromString(fileName));
	}

	public InternalOutputStream(final File file) throws FileNotFoundException {
		this.os = new RandomAccessFile(file, READ_WRITE_MODE);
		this.file = file;
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

	public long write(final byte[] buffer, final int offset, final int size) throws IOException {
		long oldPos = this.os.getFilePointer();
		this.os.write(buffer, offset, size);
		return getOffset() - oldPos;
	}

	public long write(ASInputStream stream) throws IOException {
		byte[] buf = new byte[ASBufferingInFilter.BF_BUFFER_SIZE];
		int read = stream.read(buf, buf.length);
		int res = 0;
		while (read != -1) {
			this.write(buf, 0, read);
			res += read;
			read = stream.read(buf, buf.length);
		}
		return res;
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

	public File getFile() {
		return this.file;
	}

	private static File getFileFromString(String fileName) throws FileNotFoundException {
		File file = new File(fileName);
		//check if file already exists and delete it
		if (file.exists()) {
			if (!file.delete()) {
				throw new RuntimeException("Cannot create file : " + fileName);
			}
		}
		return file;
	}
}
