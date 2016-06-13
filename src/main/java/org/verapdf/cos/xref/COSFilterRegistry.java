package org.verapdf.cos.xref;

import org.apache.log4j.Logger;
import org.verapdf.as.ASAtom;
import org.verapdf.as.exceptions.StringExceptions;
import org.verapdf.as.filters.ASFilterFactory;
import org.verapdf.as.filters.ASInFilter;
import org.verapdf.as.filters.ASOutFilter;
import org.verapdf.as.filters.IASFilterFactory;
import org.verapdf.as.io.ASInputStream;
import org.verapdf.as.io.ASOutputStream;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Timur Kamalov
 */
public class COSFilterRegistry {

	private static Map<ASAtom, IASFilterFactory> registeredFactories;
	private static final Logger LOGGER = Logger.getLogger(COSFilterRegistry.class);

	static {
		registeredFactories = new HashMap<>();
		try {
			registerFactory(ASAtom.FLATE_DECODE, new ASFilterFactory(ASAtom.FLATE_DECODE));
		} catch (Exception e) {
			LOGGER.warn("Trying to register factory twice", e);
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
		} else {
			registeredFactories.put(filterName, factory);
		}
	}

	public static ASInFilter getDecodeFilter(final ASAtom filterName,
											 final ASInputStream inputStream) throws IOException {
		final IASFilterFactory filterFactory = factoryByName(filterName);
		if (filterFactory != null) {
			return filterFactory.getInFilter(inputStream);
		} else {
			return null;
		}
	}

	public static ASOutFilter getEncodeFilter(final ASAtom filterName,
											  ASOutputStream outputStream) throws IOException {
		final IASFilterFactory filterFactory = factoryByName(filterName);
		if (filterFactory != null) {
			return filterFactory.getOutFilter(outputStream);
		} else {
			return null;
		}
	}

}
