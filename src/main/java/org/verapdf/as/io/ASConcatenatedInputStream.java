/**
 * This file is part of veraPDF Parser, a module of the veraPDF project.
 * Copyright (c) 2015-2024, veraPDF Consortium <info@verapdf.org>
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

import org.verapdf.as.CharTable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Maksim Bezrukov
 */
public class ASConcatenatedInputStream extends ASInputStream {

	private final List<ASInputStream> streams;

	private int index = 0;

	public ASConcatenatedInputStream(List<ASInputStream> streams) {
		this.streams = new ArrayList<>(streams);
		incrementResourceUsers();
	}

	@Override
	public int read() throws IOException {
		if (index == streams.size()) {
			return -1;
		}

		int newByte = streams.get(index).read();
		if (newByte != -1) {
			return newByte;
		} else {
			return ++index == streams.size() ? -1 : CharTable.ASCII_CR;
		}
	}

	@Override
	public int read(byte[] buffer, int size) throws IOException {
		if (buffer.length < size) {
			throw new IOException("Can't write bytes into passed buffer: too small.");
		}
		if (index == streams.size() || size < 0) {
			return -1;
		}
		int bufferIndex = 0;
		int left = size;
		while(left != 0 && index != streams.size()) {
			byte[] temp = new byte[left];
			int read = streams.get(index).read(temp, left);
			if (read > 0) {
				System.arraycopy(temp, 0, buffer, bufferIndex, read);
				bufferIndex += read;
				left -= read;
			}
			if (left != 0 && ++index != streams.size()) {
				buffer[bufferIndex++] = CharTable.ASCII_CR;
				left -= 1;
			}
		}

		return size - left;
	}

	@Override
	public int skip(int size) throws IOException {
		if (index == streams.size() || size <= 0) {
			return 0;
		}
		int left = size;
		while(left != 0 && index != streams.size()) {
			int skipped = streams.get(index).skip(left);
			left -= skipped;
			if (left > 0 && ++index != streams.size()) {
				left -= 1;
			}
		}

		return size - left;
	}

	@Override
	public void reset() throws IOException {
		for (ASInputStream as : streams) {
			as.reset();
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
