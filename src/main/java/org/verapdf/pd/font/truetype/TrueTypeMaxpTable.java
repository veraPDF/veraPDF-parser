package org.verapdf.pd.font.truetype;

import org.verapdf.io.InternalInputStream;

import java.io.IOException;

/**
 * @author Sergey Shemyakov
 */
class TrueTypeMaxpTable extends TrueTypeTable {

    private int numGlyphs;

    TrueTypeMaxpTable(InternalInputStream source, long offset) {
        super(source, offset);
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
