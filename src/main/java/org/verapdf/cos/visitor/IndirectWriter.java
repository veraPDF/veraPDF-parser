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
package org.verapdf.cos.visitor;

import org.verapdf.cos.COSDocument;
import org.verapdf.cos.COSKey;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Timur Kamalov
 */
public class IndirectWriter extends Writer {

	private final Map<COSKey, COSKey> renum;

	public IndirectWriter(COSDocument document, String filename, boolean append,
						  long indirectOffset) throws IOException {
		super(document, filename, append, indirectOffset);
		this.renum = new HashMap<>();
		renum.put(new COSKey(0, 65535), new COSKey(0, 65535));
	}

	@Override
	protected COSKey getKeyToWrite(final COSKey key) {
		if (!this.renum.containsKey(key)) {
			this.renum.put(key, new COSKey(this.renum.size(), 0));
		}
		return this.renum.get(key);
	}

}
