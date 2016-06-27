package org.verapdf.io;

import org.verapdf.as.filters.io.ASBufferingInFilter;
import org.verapdf.as.io.ASInputStream;

import java.io.*;

/**
 * @author Timur Kamalov
 */
public class InternalInputStream implements ASInputStream {

	private final static String READ_ONLY_MODE = "r";

	private RandomAccessFile source;

	public InternalInputStream(final String fileName) throws FileNotFoundException {
		this.source = new RandomAccessFile(fileName, READ_ONLY_MODE);
	}

	public InternalInputStream(final InputStream fileStream) throws IOException {
		this.source = new RandomAccessFile(createTempFile(fileStream), READ_ONLY_MODE);
	}

	public InternalInputStream(final ASInputStream asInputStream) throws IOException {
		this.source = new RandomAccessFile(createTempFile(asInputStream), READ_ONLY_MODE);
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

	public long getOffset() throws IOException {
		return this.source.getFilePointer();
	}

	public InternalInputStream seek(final long pos) throws IOException {
		this.source.seek(pos);
		return this;
	}

	public InternalInputStream seekFromEnd(final long pos) throws IOException {
		final long size = this.source.length();
		this.source.seek(size - pos);
		return this;
	}

	public InternalInputStream seekFromCurrentPosition(final long pos) throws IOException {
		this.source.seek(getOffset() + pos);
		return this;
	}

	public byte read() throws IOException {
		return this.source.readByte();
	}

	public byte peek() throws IOException {
		byte result = this.source.readByte();
		unread();
		return result;
	}

	public InternalInputStream unread() throws IOException{
		this.source.seek(this.source.getFilePointer() - 1);
		return this;
	}

	public InternalInputStream unread(final int count) throws IOException{
		this.source.seek(this.source.getFilePointer() - count);
		return this;
	}

	public long getStreamLength() throws IOException {
		return this.source.length();
	}

	public boolean isEof() throws IOException {
		return this.source.getFilePointer() == this.source.length();
	}

	public RandomAccessFile getStream() {
		return this.source;
	}

	private File createTempFile(InputStream input) throws IOException {
		FileOutputStream output = null;
		try {
			File tmpFile = File.createTempFile("tmp_pdf_file", ".pdf");
			output = new FileOutputStream(tmpFile);

			//copy stream content
			byte[] buffer = new byte[4096];
			int n;
			while ((n = input.read(buffer)) != -1) {
				output.write(buffer, 0, n);
			}

			return tmpFile;
		}
		finally {
			input.close();
			if (output != null) {
				output.close();
			}
		}
	}

	private File createTempFile(ASInputStream input) throws IOException {
		FileOutputStream output = null;
		try {
			File tmpFile = File.createTempFile("tmp_pdf_file", ".pdf");
			output = new FileOutputStream(tmpFile);

			//copy stream content
			byte[] buffer = new byte[ASBufferingInFilter.BF_BUFFER_SIZE];
			int n;
			while ((n = input.read(buffer, buffer.length)) != -1) {
				output.write(buffer, 0, n);
			}

			return tmpFile;
		}
		finally {
			input.close();
			if (output != null) {
				output.close();
			}
		}
	}

}
