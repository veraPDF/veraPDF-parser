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

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class does parsing of True Type "post" table and extracts all the data
 * needed.
 *
 * @author Sergey Shemyakov
 */
class TrueTypePostTable extends TrueTypeTable {

    private static final Logger LOGGER = Logger.getLogger(TrueTypePostTable.class.getCanonicalName());
    private final long length;
    private int numGlyphs;
    private final Map<String, Integer> stringToGid;

    TrueTypePostTable(SeekableInputStream source, long offset, long length) {
        super(source, offset);
        this.length = length;
        stringToGid = new HashMap<>();
    }

    void setNumGlyphs(int numGlyphs) {
        this.numGlyphs = numGlyphs;
    }

    @Override
    void readTable() throws IOException {
        long startingOffset = this.source.getOffset();
        this.source.seek(this.offset);
        float format = this.readFixed();
        this.source.skip(4 +             // italicAngle
                2 +             // underlinePosition
                2 +             // underlineThickness
                4 +             // isFixedPitch
                4 +             // minMemType42
                4 +             // maxMemType42
                4 +             // minMemType1
                4               // maxMemType1
        );
        if (format == 2.0f) {
            int numGlyphs = this.readUShort();
            if (this.numGlyphs != numGlyphs) {
                setNumGlyphs(numGlyphs);
                LOGGER.log(Level.WARNING, "Embedded TrueType font program is incorrect: numberOfGlyphs field of 'post' table does not match numGlyphs field of the 'maxp' table");
            }
            int[] glyphNameIndexInt = new int[this.numGlyphs];
            for (int i = 0; i < this.numGlyphs; ++i) {
                glyphNameIndexInt[i] = this.readUShort();
            }
            List<String> strings = new LinkedList<>();
            while (this.source.getOffset() < this.offset + this.length) {
                strings.add(this.readPascalString());
            }
            for (int i = 0; i < this.numGlyphs; ++i) {
                if (glyphNameIndexInt[i] < TrueTypePredefined.MAC_INDEX_TO_GLYPH_NAME.length) {
                    stringToGid.put(TrueTypePredefined.MAC_INDEX_TO_GLYPH_NAME[glyphNameIndexInt[i]], i);
                } else {
                    int index = glyphNameIndexInt[i] -
                            TrueTypePredefined.MAC_INDEX_TO_GLYPH_NAME.length;
                    if (index >= 0 && index < strings.size()) {
                        stringToGid.put(strings.get(index), i);
                    }
                }
            }
        } else if (format == 2.5f) {
            try {
                for (int i = 0; i < this.numGlyphs; ++i) {
                    int tableOffset = this.readChar();
                    String glyphName =
                            TrueTypePredefined.MAC_INDEX_TO_GLYPH_NAME[tableOffset + i];
                    this.stringToGid.put(glyphName, i);
                }
            } catch (ArrayIndexOutOfBoundsException e) {
                throw new IOException("Error in reading post table", e);
            }
        }
        this.source.seek(startingOffset);
    }

    int getGID(String s) {
        Integer res = this.stringToGid.get(s);
        return res == null ? 0 : res;    // gid for .notdef
    }

    boolean containsGlyph(String glyphName) {
        return this.stringToGid.containsKey(glyphName);
    }

    private String readPascalString() throws IOException {
        int length = this.readByte();
        byte[] str = new byte[length];
        this.source.read(str, length);
        return new String(str, StandardCharsets.ISO_8859_1);
    }
}
