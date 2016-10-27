package org.verapdf.pd.font.truetype;

import org.verapdf.as.io.ASInputStream;
import org.verapdf.pd.font.FontProgram;

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
    protected boolean isFontParsed = false;

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
        if (!isFontParsed) {
            isFontParsed = true;
            this.parser.readHeader();
            this.parser.readTableDirectory();
            this.parser.readTables();

            float quotient = 1000f / this.parser.getHeadParser().getUnitsPerEm();
            int[] unconvertedWidths = this.parser.getHmtxParser().getLongHorMetrics();
            widths = new float[unconvertedWidths.length];
            for (int i = 0; i < unconvertedWidths.length; ++i) {
                widths[i] = unconvertedWidths[i] * quotient;
            }
        }
    }

    /**
     * @return array, containing platform ID and encoding ID for each cmap in
     * this True Type font.
     */
    public TrueTypeCmapSubtable[] getCmapEncodingPlatform() {
        if(this.parser.getCmapParser() != null) {
            return this.parser.getCmapParser().getCmapInfos();
        } else {
            return new TrueTypeCmapSubtable[0];
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
}
