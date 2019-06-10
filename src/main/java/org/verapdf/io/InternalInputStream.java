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
import org.verapdf.as.io.ASInputStream;
import org.verapdf.tools.IntReference;

import java.io.*;

/**
 * ASInputStream for reading data from file.
 * It contains methods for file closing management.
 *
 * @author Timur Kamalov
 */
public class InternalInputStream extends SeekableInputStream {

	private final static String READ_ONLY_MODE = "r";
	private static int DEFAULT_BUFFER_SIZE = 2048;

	private RandomAccessFile stream;
	private byte[] buffer;

	private long bufferFrom;
	private long bufferTo;
	private long offset;

	private boolean isTempFile;
	private IntReference numOfFileUsers;
	private String filePath;
	private long fromOffset;
	private long size;

	public InternalInputStream(final File file) throws IOException {
		this(file, false);
	}

	public InternalInputStream(final File file, boolean isTempFile) throws IOException {
		this(file, 0, isTempFile);
	}

	public InternalInputStream(final File file, int numOfFileUsers, boolean isTempFile) throws IOException {
		this(new RandomAccessFile(file, READ_ONLY_MODE), 0, Long.MAX_VALUE,
		     new IntReference(numOfFileUsers), file.getAbsolutePath(), isTempFile);
	}

	public InternalInputStream(final String fileName) throws IOException {
		this(fileName, 0);
	}

	public InternalInputStream(final String fileName, int numOfFileUsers) throws IOException {
		this(new RandomAccessFile(fileName, READ_ONLY_MODE), 0, Long.MAX_VALUE,
		     new IntReference(numOfFileUsers), fileName, false);
	}

	public InternalInputStream(final RandomAccessFile stream, long fromOffset, long size,
	                           IntReference numOfFileUsers, String filePath, boolean isTempFile) throws IOException {
		this(stream, fromOffset, size, numOfFileUsers, filePath, isTempFile, DEFAULT_BUFFER_SIZE);
	}

	public InternalInputStream(final RandomAccessFile stream, long fromOffset, long size,
	                           IntReference numOfFileUsers, String filePath,
	                           boolean isTempFile, int bufferSize) throws IOException {
		this.stream = stream;
		this.buffer = new byte[bufferSize];
		this.bufferFrom = 0;
		this.bufferTo = 0;
		this.offset = 0;

		this.isTempFile = isTempFile;
		this.numOfFileUsers = numOfFileUsers;
		this.numOfFileUsers.increment();
		this.filePath = filePath;
		this.fromOffset = fromOffset;

		long streamLeft = stream.length() - fromOffset;
		if (streamLeft < 0) {
			throw new IOException("Offset is greater than full stream size");
		}
		this.size = size < 0 ? streamLeft : Math.min(size, streamLeft);
	}

	/**
	 * Constructor writes into temp file passed buffer, then passed stream.
	 * After that, InternalInputStream from file is created.
	 *
	 * @param alreadyRead is byte array of data that was already read from the
	 *                       beginning of stream.
	 * @param stream is data left in stream.
     */
	public static InternalInputStream createConcatenated(byte[] alreadyRead,
	                                                     final InputStream stream) throws IOException {
		File temp = createTempFile(alreadyRead, stream);
		return new InternalInputStream(temp, true);
	}

	@Override
	public int read() throws IOException {
		checkClosed("Reading");
		if (isStreamEnd()) {
			return -1;
		}

		int res = buffer[(int) (offset - bufferFrom)];
		offset++;
		return res & 0xFF;
	}

	@Override
	public int read(byte[] buffer, int size) throws IOException {
		checkClosed("Reading");
		if (buffer.length < size) {
			throw new IllegalArgumentException("Destination buffer size is less than size to be read");
		}

		int curPos = 0;
		int left = size;
		while (left > 0) {
			int read = append(buffer, curPos, left);
			if (read == -1) {
				break;
			}
			curPos += read;
			left -= read;
		}

		return curPos == 0 ? -1 : curPos;
	}

	private int append(byte[] buffer, int from, int size) throws IOException {
		if (isStreamEnd()) {
			return -1;
		}
		int toBeRead = Math.min(size, (int) (bufferTo - offset));
		System.arraycopy(this.buffer, (int) (offset - bufferFrom), buffer, from, toBeRead);
		offset += toBeRead;
		return toBeRead;
	}

	@Override
	public int skip(int size) throws IOException {
		checkClosed("Skipping");
		long newOffset = Math.min(offset + size, getStreamLength());
		int skipped = (int) (newOffset - offset);
		seek(newOffset);
		return skipped;
	}

	@Override
	public void reset() throws IOException {
		checkClosed("Reset");
		this.seek(0);
	}

	@Override
	public void seek(long offset) throws IOException {
		checkClosed("Seeking");
		if (offset > this.getStreamLength()) {
			throw new IllegalArgumentException("Destination offset is greater than stream length");
		}
		this.offset = offset < 0 ? 0 : offset;
	}

	@Override
	public int peek() throws IOException {
		checkClosed("Peeking");
		int res = read();
		if (res != -1) {
			unread();
		}
		return res;
	}

	@Override
	public long getOffset() throws IOException {
		checkClosed("Offset obtaining");
		return this.offset;
	}

	@Override
	public ASInputStream getStream(long startOffset, long length) throws IOException {
		return new InternalInputStream(this.stream, startOffset, length, numOfFileUsers, filePath, isTempFile);
	}

	@Override
	public void closeResource() throws IOException {
		if (!isSourceClosed) {
			isSourceClosed = true;
			this.numOfFileUsers.decrement();
			if (this.numOfFileUsers.equals(0)) {
				this.stream.close();
				if (isTempFile) {
					File tmp = new File(filePath);
					if (!tmp.delete()) {
						tmp.deleteOnExit();
					}
				}
			}
		}
	}

	private boolean isStreamEnd() throws IOException {
		if ((offset >= bufferFrom) && (offset < bufferTo)) {
			return false;
		}
		int read = feedBuffer();
		this.bufferFrom = offset;
		this.bufferTo = read == -1 ? offset : offset + read;
		return read <= 0;
	}

	private void checkClosed(String streamUsage) throws IOException {
		if (isSourceClosed) {
			throw new IOException(streamUsage + " can't be performed; stream is closed");
		}
	}

	private int feedBuffer() throws IOException {
		long left = getStreamLength() - offset;
		if (left <= 0) {
			return -1;
		}

		long realOffset = fromOffset + offset;
		if (this.stream.getFilePointer() != realOffset) {
			this.stream.seek(realOffset);
		}
		int read = this.stream.read(this.buffer);
		return (int) Math.min(read, left);
	}

    @Override
	public long getStreamLength() throws IOException {
		checkClosed("Stream length obtaining");
		return size;
	}

	private static File createTempFile(byte[] alreadyRead, InputStream input) throws IOException {
		File tmpFile = File.createTempFile("tmp_pdf_file", ".pdf");
		try (FileOutputStream output = new FileOutputStream(tmpFile)) {
			output.write(alreadyRead);

			//copy stream content
			byte[] buffer = new byte[ASBufferedInFilter.BF_BUFFER_SIZE];
			int n;
			while ((n = input.read(buffer, 0, ASBufferedInFilter.BF_BUFFER_SIZE)) != -1) {
				output.write(buffer, 0, n);
			}

			return tmpFile;
		} catch (IOException e) {
			if (!tmpFile.delete()) {
				tmpFile.deleteOnExit();
			}
			throw e;
		}
	}
}
