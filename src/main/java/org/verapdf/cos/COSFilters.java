/**
 * This file is part of veraPDF Parser, a module of the veraPDF project.
 * Copyright (c) 2015-2025, veraPDF Consortium <info@verapdf.org>
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
package org.verapdf.cos;

import org.verapdf.as.ASAtom;
import org.verapdf.as.io.ASInputStream;
import org.verapdf.as.io.ASOutputStream;
import org.verapdf.cos.filters.COSFilterRegistry;
import org.verapdf.pd.PDObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Timur Kamalov
 */
public class COSFilters extends PDObject {

	private static final Logger LOGGER = Logger.getLogger(COSFilters.class.getCanonicalName());

	private final List<ASAtom> entries;

	public COSFilters() {
		super();
		this.entries = new ArrayList<>();
	}

	public COSFilters(final COSObject object) {
		this();
		setObject(object);
	}

	public ASInputStream getInputStream(ASInputStream inputStream,
										COSObject decodeParams) throws IOException {
		List<COSDictionary> decodeParameters = null;
		if (!decodeParams.empty()) {
			if (decodeParams.getType() == COSObjType.COS_DICT) {
				decodeParameters = new ArrayList<>(1);
				decodeParameters.add((COSDictionary) decodeParams.getDirectBase());
			} else if (decodeParams.getType() == COSObjType.COS_ARRAY) {
				decodeParameters = new ArrayList<>(decodeParams.size());
				for (int i = 0; i < decodeParams.size(); ++i) {
					COSObjType paramsType = decodeParams.at(i).getType();
					if (decodeParams.at(i).empty() || paramsType == COSObjType.COS_NULL) {
						decodeParameters.add((COSDictionary) COSDictionary.construct().get());
					} else if (paramsType != COSObjType.COS_DICT) {
						throw new IOException("DecodeParams shall be a dictionary or array of dictionaries.");
					} else {
						decodeParameters.add((COSDictionary) decodeParams.at(i).getDirectBase());
					}
				}
			}
		}
		if (decodeParameters == null) {
			decodeParameters = new ArrayList<>(entries.size());
			for (int i = 0; i < entries.size(); ++i) {
				decodeParameters.add((COSDictionary) COSDictionary.construct().get());
			}
		}
		if (!entries.isEmpty() && decodeParameters.size() != entries.size()) {
			throw new IOException( "Amount of DecodeParams dictionaries and " +
					"amount of decode filters in COSStream are not equal.");
		}
		for (int i = 0; i < entries.size(); ++i) {
			inputStream = COSFilterRegistry.getDecodeFilter(entries.get(i),
					inputStream, decodeParameters.get(i));

			//TODO : if (!is.Get()) break;
		}
		return inputStream;
	}

	public ASOutputStream getOutputStream(ASOutputStream outputStream) throws IOException {
		for (ASAtom asAtom : entries) {
			outputStream = COSFilterRegistry.getEncodeFilter(asAtom, outputStream);

			//TODO : if (!is.Get()) break;
		}
		return outputStream;
	}

	public int size() {
		return this.entries.size();
	}

	public List<ASAtom> getFilters() {
		return entries;
	}

	@Override
	protected void updateToObject() {
		COSObject filters = getObject();

		filters.clearArray();

		for (ASAtom entry : this.entries) {
			filters.add(COSName.construct(entry));
		}
	}

	@Override
	protected void updateFromObject() {
		COSObject filters = getObject();
		if (filters.getType() == COSObjType.COS_ARRAY) {
			int size = filters.size();

			this.entries.clear();

			for (int i = 0; i < size; i++) {
				this.entries.add(filters.at(i).getName());
			}
		} else if (filters.getType() == COSObjType.COS_NAME) {
			this.entries.clear();
			this.entries.add(filters.getName());
		}
	}

}
