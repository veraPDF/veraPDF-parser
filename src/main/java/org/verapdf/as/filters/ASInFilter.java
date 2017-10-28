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
package org.verapdf.as.filters;

import org.verapdf.as.io.ASInputStream;

import java.io.IOException;

/**
 * Base class for input filters.
 *
 * @author Timur Kamalov
 */
public abstract class ASInFilter extends ASInputStream {

	private ASInputStream storedInStream;

	/**
	 * Constructor from encoded stream.
	 * @param inputStream is stream with initial encoded data.
	 * @throws IOException
     */
	protected ASInFilter(ASInputStream inputStream) {
		inputStream.incrementResourceUsers();
		this.storedInStream = inputStream;
	}

	protected ASInFilter(final ASInFilter filter) {
		if (filter != null) {
			filter.incrementResourceUsers();
			this.storedInStream = filter;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int read() throws IOException {
		byte[] temp = new byte[1];
		if (this.read(temp, 1) != -1) {
			return temp[0] & 0xFF;
		}
		return -1;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int read(byte[] buffer, int size) throws IOException {
		return this.storedInStream != null ? this.storedInStream.read(buffer, size) : -1;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int read(byte[] buffer) throws IOException {
		return this.read(buffer, buffer.length);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int skip(int size) throws IOException {
		return this.storedInStream != null ? this.storedInStream.skip(size) : 0;
	}

	public void closeResource() throws IOException {
		this.storedInStream.closeResource();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void reset() throws IOException {
		if (this.storedInStream != null) {
			this.storedInStream.reset();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void close() throws IOException {
		if (this.storedInStream != null && !isClosed) {
			isClosed = true;
			decrementResourceUsers();
			this.storedInStream.close();
		}
	}

	protected ASInputStream getInputStream() {
		return this.storedInStream;
	}

	protected void setInputStream(ASInputStream inputStream) {
		this.storedInStream = inputStream;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void incrementResourceUsers() {
		this.storedInStream.incrementResourceUsers();
	}

	@Override
	public void decrementResourceUsers() {
		this.storedInStream.decrementResourceUsers();
	}
}
