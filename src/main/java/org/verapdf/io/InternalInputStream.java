package org.verapdf.io;

import org.verapdf.as.filters.io.ASBufferingInFilter;
import org.verapdf.as.io.ASFileInStream;
import org.verapdf.as.io.ASInputStream;

import java.io.*;

/**
 * @author Timur Kamalov
 */
public class InternalInputStream extends SeekableInputStream {

	private final static String READ_ONLY_MODE = "r";

	private String fileName;
	private RandomAccessFile source;

	public InternalInputStream(final File file) throws FileNotFoundException {
		this.source = new RandomAccessFile(file, READ_ONLY_MODE);
	}

	public InternalInputStream(final String fileName) throws FileNotFoundException {
		this.fileName = fileName;
		this.source = new RandomAccessFile(fileName, READ_ONLY_MODE);
	}

	public InternalInputStream(final InputStream fileStream) throws IOException {
		File tempFile = createTempFile(fileStream);
		this.fileName = tempFile.getAbsolutePath();
		this.source = new RandomAccessFile(tempFile, READ_ONLY_MODE);
	}

	/**
	 * Constructor writes into temp file passed buffer, then passed stream.
	 * After that, InternalInputStream from file is created.
	 *
	 * @param alreadyRead is byte array of data that was already read from the
	 *                       beginning of stream.
	 * @param stream is data left in stream.
     */
	public InternalInputStream(byte[] alreadyRead, final InputStream stream)
			throws IOException {
		this.source = new RandomAccessFile(createTempFile(alreadyRead, stream),
				READ_ONLY_MODE);
	}

	@Override
	public int read() throws IOException {
		return this.source.read();
	}

	@Override
	public int read(byte[] buffer, int size) throws IOException {
		return this.source.read(buffer, 0, size);
	}

    @Override
    public int skip(int size) throws IOException {
		return this.source.skipBytes(size);
	}

    @Override
    public void close() throws IOException {
		this.source.close();
	}

    @Override
    public void reset() throws IOException {
		this.source.seek(0);
	}

	public boolean isCloneable() {
		return false;
	}

    @Override
    public long getOffset() throws IOException {
		return this.source.getFilePointer();
	}

    @Override
    public void seek(final long pos) throws IOException {
		this.source.seek(pos);
	}

    @Override
	public int peek() throws IOException {
		if (!this.isEOF()) {
			byte result = this.source.readByte();
			unread();
			return result;
		}
		return -1;
	}

    @Override
	public long getStreamLength() throws IOException {
		return this.source.length();
	}

	public String getFileName() {
		return fileName;
	}

	public RandomAccessFile getStream() {
		return this.source;
	}

	private File createTempFile(InputStream input) throws IOException {
		FileOutputStream output = null;
		try {
			File tmpFile = File.createTempFile("tmp_pdf_file", ".pdf");
			tmpFile.deleteOnExit();
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

	private File createTempFile(byte[] alreadyRead, InputStream input) throws IOException {
		FileOutputStream output = null;
		try {
			File tmpFile = File.createTempFile("tmp_pdf_file", ".pdf");
			tmpFile.deleteOnExit();
			output = new FileOutputStream(tmpFile);
			output.write(alreadyRead);

			//copy stream content
			byte[] buffer = new byte[ASBufferingInFilter.BF_BUFFER_SIZE];
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

	@Override
	public ASInputStream getStream(long startOffset, long length) {
		return new ASFileInStream(this.source, startOffset, length);
	}
}
