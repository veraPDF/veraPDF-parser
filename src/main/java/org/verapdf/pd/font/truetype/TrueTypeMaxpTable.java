package org.verapdf.pd.font.truetype;

import org.verapdf.io.SeekableInputStream;

import java.io.IOException;

/**
 * @author Sergey Shemyakov
 */
class TrueTypeMaxpTable extends TrueTypeTable {

    private int numGlyphs;

    TrueTypeMaxpTable(SeekableInputStream source, long offset) {
        super(source, offset);
    }

    public TrueTypeMaxpTable(int numGlyphs) {
        this.numGlyphs = numGlyphs;
    }

    @Override
    void readTable() throws IOException {
        long startingOffset = this.source.getOffset();
        this.source.seek(this.offset);
        this.source.skip(4);    // version
        numGlyphs = this.readUShort();
        this.source.seek(startingOffset);
    }

    int getNumGlyphs() {
        return numGlyphs;
    }
}
