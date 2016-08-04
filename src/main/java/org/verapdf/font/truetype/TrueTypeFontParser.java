package org.verapdf.font.truetype;

import org.verapdf.as.io.ASInputStream;

import java.io.IOException;

/**
 * This class does high-level parsing of True Type Font file.
 *
 * @author Sergey Shemyakov
 */
class TrueTypeFontParser extends TrueTypeBaseParser {

    private static final long HHEA = 1751672161;    // "hhea" read as 4-byte unsigned number
    private static final long HMTX = 1752003704;    // "hmtx" read as 4-byte unsigned number
    private static final long CMAP = 1668112752;    // "cmap" read as 4-byte unsigned number
    private static final long HEAD = 1751474532;    // "head" read as 4-byte unsigned number

    private int numTables;
    private TrueTypeHeadTableParser headParser;
    private TrueTypeHheaTableParser hheaParser;
    private TrueTypeHmtxTableParser hmtxParser;
    private TrueTypeCmapTableParser cmapParser;

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
            this.readULong();   // length
            if (tabName == TrueTypeFontParser.CMAP) {
                this.cmapParser = new TrueTypeCmapTableParser(this.source, offset);
            } else if (tabName == TrueTypeFontParser.HHEA) {
                this.hheaParser = new TrueTypeHheaTableParser(this.source, offset);
            } else if (tabName == TrueTypeFontParser.HMTX) {
                this.hmtxParser = new TrueTypeHmtxTableParser(this.source, offset);
            } else if (tabName == TrueTypeFontParser.HEAD) {
                this.headParser = new TrueTypeHeadTableParser(this.source, offset);
            }
        }
    }

    void readTables() throws IOException {
        this.headParser.readTable();
        this.hheaParser.readTable();
        this.hmtxParser.setNumberOfHMetrics(hheaParser.getNumberOfHMetrics());
        this.hmtxParser.readTable();
        this.cmapParser.readTable();
    }

    TrueTypeHeadTableParser getHeadParser() {
        return headParser;
    }

    TrueTypeHheaTableParser getHheaParser() {
        return hheaParser;
    }

    TrueTypeHmtxTableParser getHmtxParser() {
        return hmtxParser;
    }

    TrueTypeCmapTableParser getCmapParser() {
        return cmapParser;
    }
}
