package org.verapdf.pd.font.cmap;

import org.verapdf.as.io.ASInputStream;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class controls CMap parsing and caches all CMaps read.
 *
 * @author Sergey Shemyakov
 */
class CMapFactory {
    private static Map<String, CMap> cMapCache = new HashMap<>();
    private static final Logger LOGGER = Logger.getLogger(CMapFactory.class.getCanonicalName());

    private CMapFactory() {
        // Do nothing here
    }

    static CMap getCMap(String name, ASInputStream cMapStream) {
        CMap res = cMapCache.get(name);
        if (res != null) {
            return res;
        }
        try {
            CMapParser parser =
                    new CMapParser(cMapStream);
            parser.parse();
            res = parser.getCMap();
        } catch (IOException e) {
            LOGGER.log(Level.FINE, "Can't parse CMap " + name + ", using default", e);
            res = new CMap();
        }
        cMapCache.put(name, res);
        return res;
    }
}
