package org.verapdf.tools;

import org.verapdf.pd.font.cmap.CMap;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Sergey Shemyakov
 */
public class StaticResources {

    private static Map<String, CMap> cMapCache = new HashMap<>();

    public static void cacheCMap(String name, CMap cMap) {
        cMapCache.put(name, cMap);
    }

    public static CMap getCMap(String name) {
        return cMapCache.get(name);
    }

    private StaticResources() {}

    public static void clear() {
        cMapCache.clear();
    }
}
