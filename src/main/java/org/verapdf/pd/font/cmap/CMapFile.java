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

import org.verapdf.as.ASAtom;
import org.verapdf.as.io.ASInputStream;
import org.verapdf.cos.COSStream;

import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class represents CMap file embedded into COSStream.
 *
 * @author Sergey Shemyakov
 */
public class CMapFile {

    private static final Logger LOGGER = Logger.getLogger(CMapFile.class.getCanonicalName());

    private CMap cMap;
    private COSStream parentStream;

    /**
     * Constructor from COSStream containing CMap.
     *
     * @param parentStream is CMap stream.
     */
    public CMapFile(COSStream parentStream) {
        this.parentStream = parentStream;
    }

    /**
     * @return the value of the WMode entry in the parent CMap dictionary.
     */
    public int getDictWMode() {
        Long wMode = this.parentStream.getIntegerKey(ASAtom.W_MODE);
        return wMode == null ? 0 : wMode.intValue();
    }

    /**
     * @return the value of the WMode entry in the embedded CMap file.
     * @throws IOException if problem with parsing CMap file occurs.
     */
    public int getWMode() {
        if (cMap == null) {
            parseCMapFile();
        }
        return cMap.getwMode();
    }

    public int getMaxCID() {
        if (cMap == null) {
            parseCMapFile();
        }
        List<CIDMappable> cidMapings = this.cMap.getCidMappings();
        int res = 0;
        for (CIDMappable cidMappable : cidMapings) {
            if (cidMappable.getMaxCID() > res) {
                res = cidMappable.getMaxCID();
            }
        }
        return res;
    }

    private void parseCMapFile() {
        String cMapName = parentStream.getStringKey(ASAtom.CMAPNAME);
        try (ASInputStream data = this.parentStream.getData(COSStream.FilterFlags.DECODE)) {
            cMap = CMapFactory.getCMap(cMapName == null ? "" : cMapName, data);
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Exception while parsing cmap file", e);
        }
    }
}
