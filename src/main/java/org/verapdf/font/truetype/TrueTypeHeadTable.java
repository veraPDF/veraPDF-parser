package org.verapdf.font.truetype;

import org.verapdf.io.InternalInputStream;

import java.io.IOException;

/**
 * This class does parsing of True Type "head" table and extracts all the data
 * needed.
 *
 * @author Sergey Shemyakov
 */
class TrueTypeHeadTable extends TrueTypeTable {

    private int unitsPerEm;

    TrueTypeHeadTable(InternalInputStream source, long offset) {
        super(source, offset);
    }

    @Override
    public void readTable() throws IOException {
        long startingOffset = this.source.getOffset();
        this.source.seek(this.offset);
        this.source.skip(18);   //  4 table version, 4 fontRevision, 4 checkSumAdjustment, 4 magicNumber, 2 flags
        this.unitsPerEm = this.readUShort();
        this.source.seek(startingOffset);
    }

    int getUnitsPerEm() {
        return unitsPerEm;
    }
}
