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
import org.verapdf.as.io.ASInputStream;
import org.verapdf.as.io.ASOutputStream;
import org.verapdf.cos.COSDictionary;
import org.verapdf.exceptions.VeraPDFParserException;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class that registers encoding and decoding filters.
 *
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
		} catch (VeraPDFParserException e) {
			LOGGER.log(Level.FINE, "Trying to register factory twice", e);
		}
	}

	//singleton
	private COSFilterRegistry() {
	}

	private static IASFilterFactory factoryByName(final ASAtom name) {
		return registeredFactories.get(name);
	}

	private static void registerFactory(final ASAtom filterName, final IASFilterFactory factory) throws VeraPDFParserException {
		if (registeredFactories.containsKey(filterName)) {
			throw new VeraPDFParserException(StringExceptions.DUPLICATE_FACTORY_NAMES);
		}
		registeredFactories.put(filterName, factory);
	}

	/**
	 * Gets decode filter from given input stream with given parameters.
	 *
	 * @param filterName is name of filter to decode stream with.
	 * @param inputStream is a stream with encoded data.
	 * @param decodeParams is dictionary with decoding parameters.
	 * @return filter with decoded data.
	 */
	public static ASInputStream getDecodeFilter(final ASAtom filterName,
											 final ASInputStream inputStream,
											 COSDictionary decodeParams) throws IOException {
		if (ASAtom.CRYPT.equals(filterName) && ASAtom.IDENTITY.equals(decodeParams.getNameKey(ASAtom.NAME))) {
			return inputStream;
		}
		final IASFilterFactory filterFactory = factoryByName(filterName);
		if (filterFactory != null) {
			return filterFactory.getInFilter(inputStream, decodeParams);
		}
		LOGGER.log(Level.SEVERE, "Unknown decode filter");
		return new ASInFilter(inputStream) {
			@Override
			public int read() {
				return -1;
			}
		};
	}

	/**
	 * Gets decode filter from given input stream.
	 *
	 * @param filterName is name of filter to encode stream with.
	 * @param outputStream is a stream with data to encode.
	 * @return filter with encoded data.
	 */
	public static ASOutFilter getEncodeFilter(final ASAtom filterName,
											  ASOutputStream outputStream) throws IOException {
		final IASFilterFactory filterFactory = factoryByName(filterName);
		if (filterFactory != null) {
			return filterFactory.getOutFilter(outputStream);
		}
		return null;
	}

}
