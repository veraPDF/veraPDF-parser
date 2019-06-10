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

import org.verapdf.cos.COSHeader;
import org.verapdf.cos.COSKey;
import org.verapdf.cos.COSObject;
import org.verapdf.cos.COSTrailer;

import java.io.Closeable;
import java.io.IOException;
import java.util.List;
import java.util.SortedSet;

/**
 * @author Timur Kamalov
 */
public interface IReader extends Closeable {

	SeekableInputStream getPDFSource();

	COSHeader getHeader();

	List<COSKey> getKeys();

	COSObject getObject(final COSKey key) throws IOException;

	COSObject getObject(final long offset) throws IOException;

	Long getOffset(final COSKey key);

	long getStartXRef();

	SortedSet<Long> getStartXRefs();

	boolean isLinearized();

	COSTrailer getTrailer();

	COSTrailer getFirstTrailer();

	COSTrailer getLastTrailer();

	long getLastTrailerOffset();

	int getGreatestKeyNumberFromXref();
}
