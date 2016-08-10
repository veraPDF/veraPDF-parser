package org.verapdf.font.truetype;

import org.verapdf.io.InternalInputStream;

import java.io.IOException;

/**
 * @author Sergey Shemyakov
 */
public class TrueTypeMaxpTable extends TrueTypeTable {

    private int numGlyphs;

    public TrueTypeMaxpTable(InternalInputStream source, long offset) {
        super(source, offset);
    }

    @Override
    public void readTable() throws IOException {
        long startingOffset = this.source.getOffset();
        this.source.seek(this.offset);
        this.source.skip(4);    // version
        numGlyphs = this.readUShort();
        this.source.seek(startingOffset);
    }

    public int getNumGlyphs() {
        return numGlyphs;
    }
}
