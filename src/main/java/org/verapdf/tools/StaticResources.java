package org.verapdf.tools;

import org.verapdf.cos.COSKey;
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

    private static Map<String, CMap> cMapCache = new HashMap<>();
    private static Map<COSKey, PDStructureNameSpace> structureNameSpaceCache = new HashMap<>();

    /**
     * Caches CMap object.
     *
     * @param name is string key for cached CMap.
     * @param cMap is CMap object for caching.
     */
    public static void cacheCMap(String name, CMap cMap) {
        cMapCache.put(name, cMap);
    }

    /**
     * Gets CMap for this string key.
     *
     * @param name is key for CMap.
     * @return cached CMap with this name or null if no CMap available.
     */
    public static CMap getCMap(String name) {
        return cMapCache.get(name);
    }

    /**
     * Caches structure name space. Key is chosen to be indirect reference key
     * of this namespace dictionary.
     *
     * @param nameSpace is PD structure name space to cache.
     */
    public static void cacheStructureNameSpace(PDStructureNameSpace nameSpace) {
        COSKey key = nameSpace.getObject().getObjectKey();
        structureNameSpaceCache.put(key, nameSpace);
    }

    /**
     * Gets cached pd structure name space.
     * @param key is COSKey of namespace to get.
     * @return cached namespace with this COSKey or null if no namespace
     * available.
     */
    public static PDStructureNameSpace getStructureNameSpace(COSKey key) {
        return structureNameSpaceCache.get(key);
    }

    private StaticResources() {
    }

    /**
     * Clears all cached static resources.
     */
    public static void clear() {
        cMapCache.clear();
        structureNameSpaceCache.clear();
    }
}
