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
package org.verapdf.as.io;


import org.verapdf.tools.IntReference;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * Class for getting a stream as a part of a file without copying a file.
 *
 * @author Timur Kamalov
 */
public class ASFileInStream extends ASInputStream {

	private RandomAccessFile stream;
	private long offset;
	private long size;
	private long curPos;
	private IntReference numOfFileUsers;
	private boolean isTempFile;
	private String filePath;

	/**
	 * Creates ASFileInStream that is a substream of RandomAccessFile stream.
	 *
	 * @param stream is file stream.
	 * @param offset is the offset ot the beginning of data.
	 * @param size is the length of data chunk.
	 * @param numOfFileUsers is amount of streams that use the passed
	 *                       RandomAccessFile as a resource. It helps to
	 *                       determine when the file stream should be closed.
	 * @param filePath is the path to file that is a resource for stream.
	 * @param isTempFile is true if the resource file is a temporary file and
	 *                   should be deleted when closing resource.
	 */
	public ASFileInStream(RandomAccessFile stream, final long offset, final long size,
						  IntReference numOfFileUsers, String filePath, boolean isTempFile) {
		this.stream = stream;
		this.offset = offset;
		this.size = size;
		this.curPos = 0;
		this.numOfFileUsers = numOfFileUsers;
		this.numOfFileUsers.increment();
		this.isTempFile = isTempFile;
		this.filePath = filePath;
	}

	@Override
	public int read() throws IOException {
		if (this.curPos < this.size) {
			long prev = stream.getFilePointer();

			stream.seek(this.offset + this.curPos);
			int result = this.stream.readByte() & 0xFF;
			curPos++;

			this.stream.seek(prev);

			return result;
		} else {
			return -1;
		}
	}

	@Override
	public int read(byte[] buffer, int sizeToRead) throws IOException {
		if (sizeToRead == 0 || this.curPos >= this.size) {
			return -1;
		}

		if (sizeToRead > this.size - this.curPos) {
			sizeToRead = (int) (this.size - this.curPos);
		}

		long prev = this.stream.getFilePointer();

		this.stream.seek(this.offset + this.curPos);

		try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {
			byte[] temp = new byte[1024];
			int n;

			while (sizeToRead > 0 && (n = this.stream.read(temp, 0, Math.min(temp.length, sizeToRead))) != -1) {
				output.write(temp, 0, n);
				sizeToRead -= n;
			}

			byte[] byteArray = output.toByteArray();
			int count = byteArray.length;
			System.arraycopy(byteArray, 0, buffer, 0, count);

			this.stream.seek(prev);
			this.curPos += count;
			return count;
		}
	}

	@Override
	public int skip(int size) throws IOException {
		if (size == 0 || this.size <= this.curPos) {
			return 0;
		}

		if (size > this.size - this.curPos) {
			size = (int) (this.size - this.curPos);
		}

		this.curPos += size;

		return size;
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

	@Override
	public void incrementResourceUsers() {
		this.resourceUsers.increment();
	}

	@Override
	public void decrementResourceUsers() {
		this.resourceUsers.decrement();
	}

	@Override
	public void reset() {
		this.curPos = 0;
	}

	public String getFilePath() {
		return filePath;
	}
}
