package org.verapdf.tools;

import org.verapdf.cos.COSKey;
import org.verapdf.pd.font.FontProgram;
import org.verapdf.pd.font.cmap.CMap;
import org.verapdf.pd.structure.PDStructureNameSpace;

import java.util.HashMap;
import java.util.Map;

/**
 * Class handles static resources that need to be reset with each parsing of
 * document.
 *
 * @author Sergey Shemyakov
 */
public class StaticResources {

    private static ThreadLocal<Map<String, CMap>> cMapCache = new ThreadLocal<>();
    private static ThreadLocal<Map<COSKey, PDStructureNameSpace>> structureNameSpaceCache = new ThreadLocal<>();
    private static ThreadLocal<Map<String, FontProgram>> cachedFonts = new ThreadLocal<>();

    /**
     * Caches CMap object.
     *
     * @param name is string key for cached CMap.
     * @param cMap is CMap object for caching.
     */
    public static void cacheCMap(String name, CMap cMap) {
        checkForNull(cMapCache, new HashMap<String, CMap>());
        cMapCache.get().put(name, cMap);
    }

    /**
     * Gets CMap for this string key.
     *
     * @param name is key for CMap.
     * @return cached CMap with this name or null if no CMap available.
     */
    public static CMap getCMap(String name) {
        checkForNull(cMapCache, new HashMap<String, CMap>());
        return StaticResources.cMapCache.get().get(name);
    }

    /**
     * Caches structure name space. Key is chosen to be indirect reference key
     * of this namespace dictionary.
     *
     * @param nameSpace is PD structure name space to cache.
     */
    public static void cacheStructureNameSpace(PDStructureNameSpace nameSpace) {
        checkForNull(structureNameSpaceCache, new HashMap<COSKey, PDStructureNameSpace>());

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
        checkForNull(structureNameSpaceCache, new HashMap<COSKey, PDStructureNameSpace>());
        return StaticResources.structureNameSpaceCache.get().get(key);
    }

    private StaticResources() {
    }

    public static void cacheFontProgram(String key, FontProgram font) {
        checkForNull(cachedFonts, new HashMap<String, FontProgram>());
        if (key != null) {
            StaticResources.cachedFonts.get().put(key, font);
        }
    }

    public static FontProgram getCachedFont(String key) {
        checkForNull(cachedFonts, new HashMap<String, FontProgram>());
        if (key == null) {
            return null;
        }
        return StaticResources.cachedFonts.get().get(key);
    }

    /**
     * Clears all cached static resources.
     */
    public static void clear() {
        if (cMapCache.get()!=null
                && structureNameSpaceCache.get() != null
                && cachedFonts.get() != null) {
            StaticResources.cMapCache.get().clear();
            StaticResources.structureNameSpaceCache.get().clear();
            StaticResources.cachedFonts.get().clear();
        }
    }

    private static void checkForNull(ThreadLocal variable, Map<?, ?> map) {
        if (variable.get() == null) {
            variable.set(map);
        }
    }
}
