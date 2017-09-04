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
package org.verapdf.pd.font.cmap;

import org.verapdf.as.io.ASInputStream;
import org.verapdf.parser.postscript.PostScriptException;
import org.verapdf.tools.StaticResources;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class controls CMap parsing and caches all CMaps read.
 *
 * @author Sergey Shemyakov
 */
class CMapFactory {
    private static final Logger LOGGER = Logger.getLogger(CMapFactory.class.getCanonicalName());

    private CMapFactory() {
        // Do nothing here
    }

    /**
     * Parses CMap from given stream with caching it.
     *
     * @param name is a string identifier of cMap that is used for caching.
     * @param cMapStream is stream with cMap data.
     * @return parsed cMap object.
     */
    static CMap getCMap(String name, ASInputStream cMapStream) {
        CMap res;
        if (!name.isEmpty()) {
            res = StaticResources.getCMap(name);
            if (res != null) {
                return res;
            }
        }
        try {
            CMapParser parser =
                    new CMapParser(cMapStream);
            parser.parse();
            res = parser.getCMap();
        } catch (IOException e) {
            LOGGER.log(Level.FINE, "Can't parse CMap " + name + ", using default", e);
            res = new CMap();
        } catch (PostScriptException e) {
            LOGGER.log(Level.FINE, "PostScript exception while parsing CMap " + name);
            res = new CMap();
        }

        StaticResources.cacheCMap(name, res);
        return res;
    }
}
