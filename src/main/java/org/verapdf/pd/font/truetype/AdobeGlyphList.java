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
package org.verapdf.pd.font.truetype;


import org.verapdf.io.SeekableInputStream;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class provides methods to work with Adobe Glyph List.
 *
 * @author Sergey Shemyakov
 */
public class AdobeGlyphList {

    private static final Logger LOGGER = Logger.getLogger(AdobeGlyphList.class.getCanonicalName());

    private static final Map<String, AGLUnicode> MAPPING =
            new HashMap<>();
    private static final String AGL_FILE = "/font/AdobeGlyphList.txt";
    private static final AGLUnicode EMPTY = new AGLUnicode(-1);

    // initializes Adobe Glyph List with values from resource file.
    static {
        try {
            Scanner scanner;
            File aglFile = new File(AdobeGlyphList.class.getResource(AGL_FILE).getFile());
            if (aglFile.exists()) {
                scanner = new Scanner(aglFile, "ISO-8859-1");
            } else {
                try (InputStream input = AdobeGlyphList.class.getResourceAsStream(AGL_FILE)) {
                    scanner = new Scanner(SeekableInputStream.getSeekableStream(input), "ISO-8859-1");
                }
            }
            try {
                while (scanner.hasNextLine()) {
                    String line = scanner.nextLine();
                    String[] words = line.split(" ");
                    int symbolCode = Integer.parseInt(words[1], 16);
                    if (words.length == 2) {
                        MAPPING.put(words[0], new AGLUnicode(symbolCode));
                    } else {
                        int[] diacritic = new int[words.length - 2];
                        for (int i = 0; i < diacritic.length; ++i) {
                            diacritic[i] = Integer.parseInt(words[i + 2], 16);
                        }
                        MAPPING.put(words[0], new AGLUnicode(symbolCode, diacritic));
                    }
                }
            } finally {
                scanner.close();
            }
        } catch (IOException e) {
            LOGGER.log(Level.FINE, "Error in opening Adobe Glyph List file", e);
        }
    }

    /**
     * Returns AGLUnicode for glyph with specified name according to Adobe Glyph
     * List.
     *
     * @param glyphName is name of required glyph.
     * @return code for requested glyph.
     */
    public static AGLUnicode get(String glyphName) {
        AGLUnicode res = MAPPING.get(glyphName);
        if (res == null) {
            LOGGER.log(Level.FINE, "Cannot find glyph " + glyphName + " in Adobe Glyph List");
            return EMPTY;
        }
        return res;
    }

    /**
     * Checks if Adobe Glyph List contains given glyph.
     *
     * @param glyphName is name of glyph to check.
     * @return true if this glyph is contained in Adobe Glyph List.
     */
    public static boolean contains(String glyphName) {
        return MAPPING.containsKey(glyphName);
    }

    public static AGLUnicode empty() {
        return EMPTY;
    }

    /**
     * This class represents entity into which Adobe Glyph List maps glyph names,
     * i. a. it contains either Unicode of symbol or Unicode of symbol and
     * Unicode of diacritic symbol.
     */
    public static class AGLUnicode {
        private final int symbolCode;
        private final int[] diacriticCodes;

        AGLUnicode(int symbolCode, int... diacriticCode) {
            this.symbolCode = symbolCode;
            this.diacriticCodes = diacriticCode;
        }

        AGLUnicode(int symbolCode) {
            this.symbolCode = symbolCode;
            this.diacriticCodes = new int[0];
        }

        /**
         * @return Unicode of symbol that is not diacritic.
         */
        public int getSymbolCode() {
            return symbolCode;
        }

        /**
         * @return array of Unicode for all diacritic glyphs.
         */
        public int[] getDiacriticCodes() {
            return diacriticCodes;
        }

        /**
         * @return true if this character has diacritic symbols.
         */
        public boolean hasDiacritic() {
            return this.diacriticCodes.length != 0;
        }

        /**
         * @return String representation of given AGLUnicode.
         */
        public String getUnicodeString() {
            int[] res = new int[diacriticCodes.length + 1];
            res[0] = symbolCode;
            System.arraycopy(diacriticCodes, 0, res, 1, diacriticCodes.length);
            return new String(res, 0, res.length);
        }
    }
}
