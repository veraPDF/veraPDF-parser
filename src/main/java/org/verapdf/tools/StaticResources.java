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
package org.verapdf.tools;

import org.verapdf.cos.COSKey;
import org.verapdf.parser.PDFFlavour;
import org.verapdf.pd.PDDocument;
import org.verapdf.pd.font.FontProgram;
import org.verapdf.pd.font.cmap.CMap;
import org.verapdf.pd.structure.PDStructureNameSpace;
import org.verapdf.tools.resource.ASFileStreamCloser;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class handles static resources that need to be reset with each parsing of
 * document.
 *
 * @author Sergey Shemyakov
 */
public class StaticResources {

	private static final Logger LOGGER = Logger.getLogger(StaticResources.class.getCanonicalName());

	private static final ThreadLocal<PDFFlavour> flavour = new ThreadLocal<>();
	private static final ThreadLocal<PDDocument> document = new ThreadLocal<>();

	private static final ThreadLocal<Map<String, CMap>> cMapCache = new ThreadLocal<>();
	private static final ThreadLocal<Map<COSKey, PDStructureNameSpace>> structureNameSpaceCache = new ThreadLocal<>();
	private static final ThreadLocal<Map<String, FontProgram>> cachedFonts = new ThreadLocal<>();

	private StaticResources() {
	}

	/**
	 * Caches CMap object.
	 *
	 * @param name is string key for cached CMap.
	 * @param cMap is CMap object for caching.
	 */
	public static void cacheCMap(String name, CMap cMap) {
		checkForNull(cMapCache);
		cMapCache.get().put(name, cMap);
	}

	public static PDDocument getDocument() {
		return document.get();
	}

	public static void setDocument(PDDocument document) {
		StaticResources.document.set(document);
	}

	/**
	 * Gets CMap for this string key.
	 *
	 * @param name is key for CMap.
	 * @return cached CMap with this name or null if no CMap available.
	 */
	public static CMap getCMap(String name) {
		checkForNull(cMapCache);
		return StaticResources.cMapCache.get().get(name);
	}

	/**
	 * Caches structure name space. Key is chosen to be indirect reference key
	 * of this namespace dictionary.
	 *
	 * @param nameSpace is PD structure name space to cache.
	 */
	public static void cacheStructureNameSpace(PDStructureNameSpace nameSpace) {
		checkForNull(structureNameSpaceCache);

		COSKey key = nameSpace.getObject().getObjectKey();
		StaticResources.structureNameSpaceCache.get().put(key, nameSpace);
	}

	/**
	 * Gets cached pd structure name space.
	 *
	 * @param key is COSKey of namespace to get.
	 * @return cached namespace with this COSKey or null if no namespace
	 * available.
	 */
	public static PDStructureNameSpace getStructureNameSpace(COSKey key) {
		checkForNull(structureNameSpaceCache);
		return StaticResources.structureNameSpaceCache.get().get(key);
	}

	public static void cacheFontProgram(String key, FontProgram font) {
		checkForNull(cachedFonts);
		if (key != null) {
			StaticResources.cachedFonts.get().put(key, font);
		} else {
			StaticResources.cachedFonts.get().put(String.valueOf(font.hashCode()), font);
		}
	}

	public static FontProgram getCachedFont(String key) {
		checkForNull(cachedFonts);
		if (key == null) {
			return null;
		}
		return StaticResources.cachedFonts.get().get(key);
	}

	/**
	 * Clears all cached static resources.
	 */
	public static void clear() {
		checkForNull(cachedFonts);
		for (FontProgram fp : cachedFonts.get().values()) {
			ASFileStreamCloser fpr = fp.getFontProgramResource();
			if (fpr != null) {
				try {
					fpr.close();
				} catch (IOException e) {
					LOGGER.log(Level.WARNING, "Exception while closing font program", e);
				}
			}
		}
		StaticResources.cMapCache.set(new HashMap<>());
		StaticResources.structureNameSpaceCache.set(new HashMap<>());
		StaticResources.cachedFonts.set(new HashMap<>());
		StaticResources.flavour.set(null);
		StaticResources.document.set(null);
	}

	private static void checkForNull(ThreadLocal variable) {
		if (variable.get() == null) {
			variable.set(new HashMap<>());
		}
	}

	public static Map<String, CMap> getcMapCache() {
		return cMapCache.get();
	}

	public static void setcMapCache(Map<String, CMap> cMapCache) {
		StaticResources.cMapCache.set(cMapCache);
	}

	public static Map<COSKey, PDStructureNameSpace> getStructureNameSpaceCache() {
		return structureNameSpaceCache.get();
	}

	public static void setStructureNameSpaceCache(Map<COSKey, PDStructureNameSpace> structureNameSpaceCache) {
		StaticResources.structureNameSpaceCache.set(structureNameSpaceCache);
	}

	public static Map<String, FontProgram> getCachedFonts() {
		return cachedFonts.get();
	}

	public static void setCachedFonts(Map<String, FontProgram> cachedFonts) {
		StaticResources.cachedFonts.set(cachedFonts);
	}

	public static PDFFlavour getFlavour() {
		return flavour.get();
	}

	public static void setFlavour(PDFFlavour flavour) {
		StaticResources.flavour.set(flavour);
	}
}
