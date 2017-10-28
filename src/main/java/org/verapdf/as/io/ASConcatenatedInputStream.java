package org.verapdf.as.io;

import org.verapdf.as.CharTable;

import java.io.IOException;

/**
 * @author Maksim Bezrukov
 */
public class ASConcatenatedInputStream extends ASInputStream {

	private final ASInputStream[] streams;
	private int index = 0;

	public ASConcatenatedInputStream(ASInputStream[] streams) {
		this.streams = streams;
		incrementResourceUsers();
	}

	@Override
	public int read() throws IOException {
		if (index == streams.length) {
			return -1;
		}

		int newByte = streams[index].read();
		if (newByte != -1) {
			return newByte;
		} else {
			return ++index == streams.length ? -1 : CharTable.ASCII_CR;
		}
	}

	@Override
	public int read(byte[] buffer, int size) throws IOException {
		if (buffer.length < size) {
			throw new IOException("Can't write bytes into passed buffer: too small.");
		}
		if (index == streams.length || size < 0) {
			return -1;
		}
		int bufferIndex = 0;
		int left = size;
		while(left != 0 && index != streams.length) {
			byte[] temp = new byte[left];
			int read = streams[index].read(temp, left);
			if (read > 0) {
				System.arraycopy(temp, 0, buffer, bufferIndex, read);
				bufferIndex += read;
				left -= read;
			}
			if (left != 0 && ++index != streams.length) {
				buffer[bufferIndex++] = CharTable.ASCII_CR;
				left -= 1;
			}
		}

		return size - left;
	}

	@Override
	public int skip(int size) throws IOException {
		if (index == streams.length || size <= 0) {
			return 0;
		}
		int left = size;
		while(left != 0 && index != streams.length) {
			int skipped = streams[index].skip(left);
			left -= skipped;
			if (left > 0 && ++index != streams.length) {
				left -= 1;
			}
		}

		return size - left;
	}

	@Override
	public void reset() throws IOException {
		for (int i = 0; i <= index; ++i) {
			streams[i].reset();
		}
		index = 0;
	}

	@Override
	public void closeResource() throws IOException {
		for (ASInputStream as : streams) {
			as.closeResource();
		}
	}

	@Override
	public void incrementResourceUsers() {
		for (ASInputStream as : streams) {
			as.incrementResourceUsers();
		}
	}

	@Override
	public void decrementResourceUsers() {
		for (ASInputStream as : streams) {
			as.decrementResourceUsers();
		}
	}

	@Override
	public void close() throws IOException {
		if (!isClosed) {
			decrementResourceUsers();
			isClosed = true;
			for (ASInputStream as : streams) {
				as.close();
			}
		}
	}
}
