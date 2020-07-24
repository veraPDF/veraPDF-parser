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
import org.verapdf.as.io.ASMemoryInStream;
import org.verapdf.pd.font.FontProgram;
import org.verapdf.tools.resource.ASFileStreamCloser;

import java.io.IOException;

/**
 * Base class for TrueTypeFontProgram and CIDFontType2Program.
 *
 * @author Sergey Shemyakov
 */
public abstract class BaseTrueTypeProgram implements FontProgram {

    protected float[] widths;

    protected TrueTypeFontParser parser;
    protected String[] encodingMappingArray;
    private boolean attemptedParsing = false;
    private boolean successfullyParsed = false;

    /**
     * Constructor from stream containing font data, and encoding details.
     *
     * @param stream     is stream containing font data.
     * @throws IOException if creation of @{link SeekableStream} fails.
     */
    public BaseTrueTypeProgram(ASInputStream stream)
            throws IOException {
        this.parser = new TrueTypeFontParser(stream);
    }

    /**
     * Parses True Type font from given stream and extracts all the data needed.
     *
     * @throws IOException if stream-reading error occurs.
     */
    @Override
    public void parseFont() throws IOException {
        if (!attemptedParsing) {
            try {
                attemptedParsing = true;
                this.parser.readHeader();
                this.parser.readTableDirectory();
                this.parser.readTables();

                float quotient = 1000f / this.parser.getHeadParser().getUnitsPerEm();
                int[] unconvertedWidths = this.parser.getHmtxParser().getLongHorMetrics();
                widths = new float[unconvertedWidths.length];
                for (int i = 0; i < unconvertedWidths.length; ++i) {
                    widths[i] = unconvertedWidths[i] * quotient;
                }
                this.successfullyParsed = true;
            } finally {
                this.parser.source.close();    // We close stream after first reading attempt
            }
        }
    }

    /**
     * @return array, containing platform ID and encoding ID for each cmap in
     * this True Type font.
     */
    public int getNrOfCMaps() {
        if(this.parser.getCmapParser() != null) {
            return this.parser.getCmapParser().getCmapInfos().length;
        } else {
            return 0;
        }
    }

    /**
     * @return number of glyphs in this font.
     */
    public int getNGlyphs() {
        return this.parser.getMaxpParser().getNumGlyphs();
    }

    /**
     * Returns true if cmap table with given platform ID and encoding ID is
     * present in the font.
     *
     * @param platformID is platform ID of requested cmap.
     * @param encodingID is encoding ID of requested cmap.
     * @return true if requested cmap is present.
     */
    public boolean isCmapPresent(int platformID, int encodingID) {
        return this.parser.getCmapTable(platformID, encodingID) != null;
    }

    public boolean isCmapPresent() {
        return this.parser.isCmapPresent();
    }

    protected float getWidthWithCheck(int gid) {
        if (gid < widths.length) {
            return widths[gid];
        } else {
            if(gid < this.parser.getMaxpParser().getNumGlyphs()) {
                return widths[widths.length - 1];   // case of monospaced fonts
            } else {
                return widths[0];
            }
        }
    }

    @Override
    public boolean isAttemptedParsing() {
        return this.attemptedParsing;
    }

    @Override
    public boolean isSuccessfulParsing() {
        return this.successfullyParsed;
    }

    @Override
    public ASFileStreamCloser getFontProgramResource() {
        if (this.parser.source instanceof ASMemoryInStream) {
            return null;
        } else {
            return new ASFileStreamCloser(this.parser.source);
        }
    }
}
