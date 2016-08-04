package org.verapdf.font.truetype;

import org.verapdf.as.io.ASInputStream;

import java.io.IOException;

/**
 * Instance of this class contains True Type Font data.
 *
 * @author Sergey Shemyakov
 */
public class TrueTypeFont {

    private float[] widths;

    private TrueTypeFontParser parser;

    /**
     * Constructor from stream, containing font data.
     *
     * @param stream is stream containing font data.
     * @throws IOException if creation of @{link InternalInputStream} fails.
     */
    public TrueTypeFont(ASInputStream stream) throws IOException {
        this.parser = new TrueTypeFontParser(stream);
    }

    /**
     * Parses True Type font from given stream and extracts all the data needed.
     *
     * @throws IOException if stream-reading error occurs.
     */
    public void parse() throws IOException {
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

    /**
     * @return array, containing platform ID for each cmap in this True Type
     * font.
     */
    public int[] getCmapPlatformIDs() {
        return this.parser.getCmapParser().getPlatformIDs();
    }

    /**
     * @return array, containing encoding ID for each cmap in this True Type
     * font.
     */
    public int[] getCmapEncodingIDs() {
        return this.parser.getCmapParser().getEncodingIDs();
    }

    /**
     * @return array, containing width for each glyph in this True Type font.
     */
    public float[] getWidths() {
        return widths;
    }
}
