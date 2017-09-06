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

import java.io.IOException;
import java.io.InputStream;

/**
 * Base class for stream hierarchy. Has methods for resource management.
 * Please don't forget to close all the opened streams.
 *
 * @author Timur Kamalov
 */
public abstract class ASInputStream extends InputStream {

	protected boolean isClosed = false;
	protected boolean isSourceClosed = false;

	protected IntReference resourceUsers = new IntReference(1);

	public abstract int read() throws IOException;

	public abstract int read(byte[] buffer, int size) throws IOException;

	public abstract int skip(int size) throws IOException;

	@Override
	public void close() throws IOException {
		if (!isClosed) {
			isClosed = true;
			if (!this.resourceUsers.equals(0)) {
				this.resourceUsers.decrement();
			}
		}
		if (this.resourceUsers.equals(0)) {
			closeResource();
		}
	}

	@Override
	public abstract void reset() throws IOException;

	/**
	 * Closes stream resource. There is a difference between closing stream and
	 * closing it's resource. Several streams may have the same resource (e. g.
	 * the same file stream) and resource should be closed only after all
	 * streams using it are closed.
	 */
	public abstract void closeResource() throws IOException;

	/**
	 * Method increments number of resource users.
	 *
	 * {@see closeResource}
	 */
	public abstract void incrementResourceUsers();

	/**
	 * Method decrements number of resource users.
	 *
	 * {@see closeResource}
	 */
	public abstract void decrementResourceUsers();

	/**
	 * Creates copy of stream. The two streams can be closed separately.
	 */
	public static ASInputStream createStreamFromStream(ASInputStream stream) {
		return new ASInputStreamWrapper(stream);
	}
}
