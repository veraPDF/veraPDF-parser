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
package org.verapdf.pd.font.stdmetrics;

import org.verapdf.as.io.ASInputStream;
import org.verapdf.io.InternalInputStream;
import org.verapdf.io.SeekableInputStream;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class that manages Adobe Font Metrics data loading.
 *
 * @author Sergey Shemyakov
 */
public class StandardFontMetricsFactory {

    private static final Logger LOGGER = Logger.getLogger(StandardFontMetricsFactory.class.getCanonicalName());
    private static final String DIR_PATH = "/font/stdmetrics/";
    private static final String EXTENSION = ".afm";
    private static final Map<String, StandardFontMetrics> FONT_METRICS_MAP =
            new HashMap<>();

    private StandardFontMetricsFactory() {}

    /**
     * Gets font metrics for the font with given name.
     */
    public static StandardFontMetrics getFontMetrics(String fontName) {
        StandardFontMetrics res = FONT_METRICS_MAP.get(fontName);
        if (res == null) {
            String afmPath = DIR_PATH + fontName + EXTENSION;
            try (ASInputStream afmStream = load(afmPath)) {
                AFMParser parser = new AFMParser(afmStream, fontName);
                res = parser.parse();
                FONT_METRICS_MAP.put(fontName, res);
            } catch (IOException e) {
                LOGGER.log(Level.FINE, "Can't open file input stream for predefined font file " + DIR_PATH + fontName + EXTENSION, e);
                return null;
            }
        }
        return res;
    }

    private static ASInputStream load(String fileName) {
        try {
            File afmFile = new File(StandardFontMetricsFactory.class.getResource(fileName).getFile());
            if (afmFile.exists()) {
                return new InternalInputStream(afmFile);
            } else {
                try (InputStream input = StandardFontMetrics.class.getResourceAsStream(fileName)) {
                    return SeekableInputStream.getSeekableStream(input);
                }
            }
        } catch (IOException e) {
            LOGGER.log(Level.FINE, "Error in opening predefined font metrics file " + fileName, e);
            return null;
        }
    }
}
