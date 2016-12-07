package org.verapdf.pd.font.truetype;

import org.verapdf.io.SeekableInputStream;

import java.io.IOException;

/**
 * This class does parsing of True Type "hhea" table and extracts all the data
 * needed.
 *
 * @author Sergey Shemyakov
 */
class TrueTypeHheaTable extends TrueTypeTable {

    private int numberOfHMetrics;

    TrueTypeHheaTable(SeekableInputStream source, long offset) {
        super(source, offset);
    }

    @Override
    void readTable() throws IOException {
        long startingOffset = this.source.getOffset();
        this.source.seek(this.offset);
        this.source.skip(4 +   // version
                2 +             // ascender
                2 +             // descender
                2 +             // line gap
                2 +             // advanceWidthMax
                2 +             // minLeftSideBearing
                2 +             // minRightSideBearing
                2 +             // xMaxExtent
                2 +             // caretSlopeRise
                2 +             // caretSlopeRun
                10 +            // reserved
                2               // metricDataFormat
        );
        this.numberOfHMetrics = this.readUShort();
        this.source.seek(startingOffset);
    }

    int getNumberOfHMetrics() {
        return numberOfHMetrics;
    }
}
