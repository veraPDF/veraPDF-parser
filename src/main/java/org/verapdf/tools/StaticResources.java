package org.verapdf.tools;

import org.verapdf.cos.COSKey;
import org.verapdf.pd.font.cmap.CMap;
import org.verapdf.pd.structure.PDStructureNameSpace;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Sergey Shemyakov
 */
public class StaticResources {

    private static Map<String, CMap> cMapCache = new HashMap<>();
    private static Map<COSKey, PDStructureNameSpace> structureNameSpaceCache = new HashMap<>();

    public static void cacheCMap(String name, CMap cMap) {
        cMapCache.put(name, cMap);
    }

    public static CMap getCMap(String name) {
        return cMapCache.get(name);
    }

    public static void cacheStructureNameSpace(PDStructureNameSpace nameSpace) {
        COSKey key = nameSpace.getObject().getObjectKey();
        structureNameSpaceCache.put(key, nameSpace);
    }

    public static PDStructureNameSpace getStructureNameSpace(COSKey key) {
        return structureNameSpaceCache.get(key);
    }

    private StaticResources() {}

    public static void clear() {
        cMapCache.clear();
        structureNameSpaceCache.clear();
    }
}
