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

import org.verapdf.as.io.ASInputStream;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class does high-level parsing of True Type Font file.
 *
 * @author Sergey Shemyakov
 */
class TrueTypeFontParser extends TrueTypeBaseParser {

    private static final Logger LOGGER = Logger.getLogger(TrueTypeFontParser.class.getCanonicalName());

    private static final long HHEA = 1751672161;    // "hhea" read as 4-byte unsigned number
    private static final long HMTX = 1752003704;    // "hmtx" read as 4-byte unsigned number
    private static final long CMAP = 1668112752;    // "cmap" read as 4-byte unsigned number
    private static final long HEAD = 1751474532;    // "head" read as 4-byte unsigned number
    private static final long POST = 1886352244;    // "post" read as 4-byte unsigned number
    private static final long MAXP = 1835104368;    // "maxp" read as 4-byte unsigned number

    private int numTables;
    private TrueTypeHeadTable headParser;
    private TrueTypeHheaTable hheaParser;
    private TrueTypeHmtxTable hmtxParser;
    private TrueTypeCmapTable cmapParser;
    private TrueTypePostTable postParser;
    private TrueTypeMaxpTable maxpParser;

    TrueTypeFontParser(ASInputStream source) throws IOException {
        super(source);
    }

    void readHeader() throws IOException {
        this.source.skip(4);   // version
        this.numTables = this.readUShort();
        this.source.skip(6);
    }

    void readTableDirectory() throws IOException {
        for (int i = 0; i < numTables; ++i) {
            long tabName = this.readULong();
            this.readULong();   // checksum
            long offset = this.readULong();
            long length = this.readULong();   // length
            if (tabName == CMAP) {
                this.cmapParser = new TrueTypeCmapTable(this.source, offset);
            } else if (tabName == HHEA) {
                this.hheaParser = new TrueTypeHheaTable(this.source, offset);
            } else if (tabName == HMTX) {
                this.hmtxParser = new TrueTypeHmtxTable(this.source, offset);
            } else if (tabName == HEAD) {
                this.headParser = new TrueTypeHeadTable(this.source, offset);
            } else if (tabName == POST) {
                this.postParser = new TrueTypePostTable(this.source, offset, length);
            } else if (tabName == MAXP) {
                this.maxpParser = new TrueTypeMaxpTable(source, offset);
            }
        }
    }

    void readTables() throws IOException {
        if (headParser != null) {
            this.headParser.readTable();
        } else {
            LOGGER.log(Level.FINE, "True type font doesn't contain head table. Default value for unitsPerEm used.");
            this.headParser = new TrueTypeHeadTable();
        }

        if (hheaParser != null) {
            this.hheaParser.readTable();
        } else {
            throw new IOException("True type font doesn't contain hhea table.");
        }

        if (hmtxParser != null) {
            this.hmtxParser.setNumberOfHMetrics(hheaParser.getNumberOfHMetrics());
            this.hmtxParser.readTable();
        } else {
            throw new IOException("True type font doesn't contain hmtx table.");
        }

        if (cmapParser != null) {
            this.cmapParser.readTable();
        } else {
            LOGGER.log(Level.FINE, "True type font doesn't contain cmap table.");
        }

        if (this.maxpParser != null) {
            this.maxpParser.readTable();
        } else {
            this.maxpParser = new TrueTypeMaxpTable(
                    this.hmtxParser.getLongHorMetrics().length);
            LOGGER.log(Level.FINE, "True type font doesn't contain maxp table. Default value for numGlyphs used.");
        }

        if (this.postParser != null) {
            this.postParser.setNumGlyphs(maxpParser.getNumGlyphs());
            this.postParser.readTable();
        } else {
            LOGGER.log(Level.FINE, "True type font doesn't contain post table.");
        }
    }

    TrueTypeHeadTable getHeadParser() {
        return headParser;
    }

    TrueTypeHmtxTable getHmtxParser() {
        return hmtxParser;
    }

    TrueTypeCmapTable getCmapParser() {
        return cmapParser;
    }

    TrueTypePostTable getPostParser() {
        return postParser;
    }

    TrueTypeMaxpTable getMaxpParser() {
        return maxpParser;
    }

    TrueTypeCmapSubtable getCmapTable(int platformID, int encodingID) {
        if (cmapParser != null) {
            for (TrueTypeCmapSubtable ttci : cmapParser.getCmapInfos()) {
                if (ttci.getPlatformID() == platformID &&
                        ttci.getEncodingID() == encodingID) {
                    return ttci;
                }
            }
        }
        return null;
    }

    TrueTypeHheaTable getHheaParser() {
        return hheaParser;
    }
}
