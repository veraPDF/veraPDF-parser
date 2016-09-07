package org.verapdf.pd.font.truetype;

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
            if (tabName == TrueTypeFontParser.CMAP) {
                this.cmapParser = new TrueTypeCmapTable(this.source, offset);
            } else if (tabName == TrueTypeFontParser.HHEA) {
                this.hheaParser = new TrueTypeHheaTable(this.source, offset);
            } else if (tabName == TrueTypeFontParser.HMTX) {
                this.hmtxParser = new TrueTypeHmtxTable(this.source, offset);
            } else if (tabName == TrueTypeFontParser.HEAD) {
                this.headParser = new TrueTypeHeadTable(this.source, offset);
            } else if (tabName == TrueTypeFontParser.POST) {
                this.postParser = new TrueTypePostTable(this.source, offset, length);
            } else if (tabName == TrueTypeFontParser.MAXP) {
                this.maxpParser = new TrueTypeMaxpTable(source, offset);
            }
        }
    }

    void readTables() throws IOException {
        this.headParser.readTable();
        this.hheaParser.readTable();
        this.hmtxParser.setNumberOfHMetrics(hheaParser.getNumberOfHMetrics());
        this.hmtxParser.readTable();
        this.cmapParser.readTable();
        this.maxpParser.readTable();
        this.postParser.setNumGlyphs(maxpParser.getNumGlyphs());
        this.postParser.readTable();
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

    public TrueTypeMaxpTable getMaxpParser() {
        return maxpParser;
    }

    TrueTypeCmapSubtable getCmapTable(int platformID, int encodingID) {
        for (TrueTypeCmapSubtable ttci : cmapParser.getCmapInfos()) {
            if (ttci.getPlatformID() == platformID &&
                    ttci.getEncodingID() == encodingID) {
                return ttci;
            }
        }
        return null;
    }
}
