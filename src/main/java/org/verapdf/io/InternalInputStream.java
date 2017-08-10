/**
 * This file is part of veraPDF Parser, a module of the veraPDF project.
 * Copyright (c) 2015, veraPDF Consortium <info@verapdf.org>
 * All rights reserved.
 *
 * veraPDF Parser is free software: you can redistribute it and/or modify
 * it under the terms of either:
 *
 * The GNU General public license GPLv3+.
 * You should have received a copy of the GNU General Public License
 * along with veraPDF Parser as the LICENSE.GPL file in the root of the source
 * tree.  If not, see http://www.gnu.org/licenses/ or
 * https://www.gnu.org/licenses/gpl-3.0.en.html.
 *
 * The Mozilla Public License MPLv2+.
 * You should have received a copy of the Mozilla Public License along with
 * veraPDF Parser as the LICENSE.MPL file in the root of the source tree.
 * If a copy of the MPL was not distributed with this file, you can obtain one at
 * http://mozilla.org/MPL/2.0/.
 */
package org.verapdf.io;

import org.verapdf.as.filters.io.ASBufferedInFilter;
import org.verapdf.as.io.ASFileInStream;
import org.verapdf.as.io.ASInputStream;
import org.verapdf.tools.IntReference;

import java.io.*;

/**
 * @author Timur Kamalov
 */
public class InternalInputStream extends SeekableInputStream {

	private final static String READ_ONLY_MODE = "r";

	private boolean isTempFile;
	private IntReference numOfFileUsers;
	private String fileName;
	private RandomAccessFile source;

	public InternalInputStream(final File file) throws FileNotFoundException {
		this(file, 1);
	}

	public InternalInputStream(final File file, int numOfFileUsers) throws FileNotFoundException {
		this.isTempFile = false;
		this.fileName = file.getAbsolutePath();
		this.source = new RandomAccessFile(file, READ_ONLY_MODE);
		this.numOfFileUsers = new IntReference(numOfFileUsers);
	}

	public InternalInputStream(final String fileName) throws FileNotFoundException {
		this(fileName, 1);
	}

	public InternalInputStream(final String fileName, int numOfFileUsers) throws FileNotFoundException {
		this.isTempFile = false;
		this.fileName = fileName;
		this.source = new RandomAccessFile(fileName, READ_ONLY_MODE);
		this.numOfFileUsers = new IntReference(numOfFileUsers);
	}

	public InternalInputStream(final InputStream fileStream) throws IOException {
		this.isTempFile = true;
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
		this.isTempFile = true;
		File temp = createTempFile(alreadyRead, stream);
		this.fileName = temp.getAbsolutePath();
		this.source = new RandomAccessFile(temp, READ_ONLY_MODE);
		this.numOfFileUsers = new IntReference(1);
	}

	@Override
	public int read() throws IOException {
		checkClosed("Reading");
		return this.source.read();
	}

	@Override
	public int read(byte[] buffer, int size) throws IOException {
		checkClosed("Reading");
		return this.source.read(buffer, 0, size);
	}

    @Override
    public int skip(int size) throws IOException {
		checkClosed("Skipping");
		return this.source.skipBytes(size);
	}

    @Override
    public void closeResource() throws IOException {
		if (!isSourceClosed) {
			isSourceClosed = true;
			this.numOfFileUsers.decrement();
			if (this.numOfFileUsers.equals(0)) {
				this.source.close();
				if (isTempFile) {
					File tmp = new File(fileName);
					tmp.delete();
				}
			}
		}
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
		checkClosed("Offset obtaining");
		return this.source.getFilePointer();
	}

    @Override
    public void seek(final long pos) throws IOException {
		checkClosed("Seeking");
		this.source.seek(pos);
	}

    @Override
	public int peek() throws IOException {
		checkClosed("Peeking");
		if (!this.isEOF()) {
			byte result = this.source.readByte();
			unread();
			return result;
		}
		return -1;
	}

    @Override
	public long getStreamLength() throws IOException {
		checkClosed("Stream length obtaining");
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
			while ((n = input.read(buffer, 0, ASBufferedInFilter.BF_BUFFER_SIZE)) != -1) {
				output.write(buffer, 0, n);
			}

			return tmpFile;
		}
		finally {
			if (output != null) {
				output.close();
			}
		}
	}

	private File createTempFile(byte[] alreadyRead, InputStream input) throws IOException {
		FileOutputStream output = null;
		File tmpFile = File.createTempFile("tmp_pdf_file", ".pdf");
		tmpFile.deleteOnExit();
		try {
			output = new FileOutputStream(tmpFile);
			output.write(alreadyRead);

			//copy stream content
			byte[] buffer = new byte[ASBufferedInFilter.BF_BUFFER_SIZE];
			int n;
			while ((n = input.read(buffer, 0, ASBufferedInFilter.BF_BUFFER_SIZE)) != -1) {
				output.write(buffer, 0, n);
			}

			return tmpFile;
		} catch (IOException e) {
			tmpFile.delete();
			throw e;
		}
		finally {
			if (output != null) {
				output.close();
			}
		}
	}

	@Override
	public ASInputStream getStream(long startOffset, long length) {
		return new ASFileInStream(this.source,
				startOffset, length, numOfFileUsers, this.fileName, this.isTempFile);
	}

	private void checkClosed(String streamUsage) throws IOException {
		if (isSourceClosed) {
			throw new IOException(streamUsage + " can't be performed; stream is closed");
		}
	}

	public boolean isSourceClosed() {
		return isSourceClosed;
	}
}
