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

import org.verapdf.as.filters.io.ASBufferedInFilter;
import org.verapdf.as.io.ASInputStream;
import org.verapdf.as.io.ASOutputStream;

import java.io.IOException;

/**
 * Base class for output filters.
 *
 * @author Timur Kamalov
 */
public abstract class ASOutFilter implements ASOutputStream {

	private ASOutputStream storedOutputStream;

	protected ASOutFilter(final ASOutputStream outStream) {
		this.storedOutputStream = outStream;
	}

	protected ASOutputStream getStoredOutputStream() {
		return this.storedOutputStream;
	}

	private ASOutFilter(final ASOutFilter filter) {
		close();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public long write(final byte[] buffer) throws IOException {
		return this.storedOutputStream != null ?
				this.storedOutputStream.write(buffer) : 0;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public long write(final byte[] buffer, int offset, int size) throws IOException {
		return this.storedOutputStream != null ?
				this.storedOutputStream.write(buffer, offset, size) : 0;
 	}

	/**
	 * {@inheritDoc}
	 */
	@Override
 	public long write(ASInputStream stream) throws IOException {
		return this.storedOutputStream != null ?
				this.storedOutputStream.write(stream) : 0;
//		byte[] buf = new byte[ASBufferedInFilter.BF_BUFFER_SIZE];
//		int read = stream.read(buf, buf.length);
//		int res = 0;
//		while (read != -1) {
//			this.write(buf, 0, read);
//			res += read;
//			read = stream.read(buf, buf.length);
//		}
//		return res;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void close() {
		this.storedOutputStream = null;
	}

}
