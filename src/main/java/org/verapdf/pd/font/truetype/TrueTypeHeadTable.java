package org.verapdf.pd.font.truetype;

import org.verapdf.io.SeekableInputStream;

import java.io.IOException;

/**
 * This class does parsing of True Type "head" table and extracts all the data
 * needed.
 *
 * @author Sergey Shemyakov
 */
class TrueTypeHeadTable extends TrueTypeTable {

    private int unitsPerEm;

    TrueTypeHeadTable(SeekableInputStream source, long offset) {
        super(source, offset);
    }

    /**
     * Sets units per em to default. Should be used if table is not present in
     * font program.
     */
    TrueTypeHeadTable() {
        this.unitsPerEm = 2048;
    }

    @Override
    void readTable() throws IOException {
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
