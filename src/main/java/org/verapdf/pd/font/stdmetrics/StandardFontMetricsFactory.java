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

import org.verapdf.as.io.ASFileInStream;
import org.verapdf.as.io.ASInputStream;
import org.verapdf.tools.IntReference;

import java.io.*;
import java.net.URL;
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
            File afmFile;
            URL res = StandardFontMetrics.class.getResource(fileName);
            if (res.toString().startsWith("jar:")) {
                InputStream input = StandardFontMetrics.class.getResourceAsStream(fileName);
                afmFile = File.createTempFile("tempfile", ".tmp");
                OutputStream out = new FileOutputStream(afmFile);
                int read;
                byte[] bytes = new byte[1024];

                while ((read = input.read(bytes)) != -1) {
                    out.write(bytes, 0, read);
                }
                input.close();
                out.close();
                afmFile.deleteOnExit();
            } else {
                afmFile = new File(res.getFile());
            }
            if (!afmFile.exists()) {
                throw new IOException("Error: File " + afmFile + " not found!");
            }
            return new ASFileInStream(
                    new RandomAccessFile(afmFile, "r"), 0, afmFile.length(),
                    new IntReference(), afmFile.getAbsolutePath(), true);
        } catch (IOException e) {
            LOGGER.log(Level.FINE, "Error in opening predefined font metrics file " + fileName, e);
            return null;
        }
    }
}
