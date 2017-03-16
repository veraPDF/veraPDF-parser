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
package org.verapdf.cos.filters;

import org.verapdf.as.ASAtom;
import org.verapdf.as.exceptions.StringExceptions;
import org.verapdf.as.filters.ASFilterFactory;
import org.verapdf.as.filters.ASInFilter;
import org.verapdf.as.filters.ASOutFilter;
import org.verapdf.as.filters.IASFilterFactory;
import org.verapdf.as.filters.io.ASBufferedInFilter;
import org.verapdf.as.io.ASInputStream;
import org.verapdf.as.io.ASOutputStream;
import org.verapdf.cos.COSDictionary;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Timur Kamalov
 */
public class COSFilterRegistry {

	private static Map<ASAtom, IASFilterFactory> registeredFactories;
	private static final Logger LOGGER = Logger.getLogger(COSFilterRegistry.class.getCanonicalName());

	static {
		registeredFactories = new HashMap<>();
		try {
			registerFactory(ASAtom.FLATE_DECODE, new ASFilterFactory(ASAtom.FLATE_DECODE));
			registerFactory(ASAtom.ASCII_HEX_DECODE, new ASFilterFactory(ASAtom.ASCII_HEX_DECODE));
			registerFactory(ASAtom.ASCII85_DECODE, new ASFilterFactory(ASAtom.ASCII85_DECODE));
			registerFactory(ASAtom.LZW_DECODE, new ASFilterFactory(ASAtom.LZW_DECODE));
		} catch (Exception e) {
			LOGGER.log(Level.FINE, "Trying to register factory twice", e);
		}
	}

	//singleton
	private COSFilterRegistry() {
	}

	private static IASFilterFactory factoryByName(final ASAtom name) {
		return registeredFactories.get(name);
	}

	public static void registerFactory(final ASAtom filterName, final IASFilterFactory factory) throws Exception {
		if (registeredFactories.containsKey(filterName)) {
			throw new Exception("COSFilterRegistry::RegisterFactory(...)" + StringExceptions.DUPLICATE_FACTORY_NAMES);
		}
		registeredFactories.put(filterName, factory);
	}

	public static ASInFilter getDecodeFilter(final ASAtom filterName,
											 final ASInputStream inputStream,
											 COSDictionary decodeParams) throws IOException {
		final IASFilterFactory filterFactory = factoryByName(filterName);
		if (filterFactory != null) {
			return filterFactory.getInFilter(inputStream, decodeParams);
		}
		LOGGER.log(Level.FINE, "Trying to use unimplemented decoding filter.");
		return new ASBufferedInFilter(inputStream);
	}

	public static ASOutFilter getEncodeFilter(final ASAtom filterName,
											  ASOutputStream outputStream) throws IOException {
		final IASFilterFactory filterFactory = factoryByName(filterName);
		if (filterFactory != null) {
			return filterFactory.getOutFilter(outputStream);
		}
		return null;
	}

}
