package org.verapdf.font.truetype;

import org.verapdf.io.InternalInputStream;

import java.io.IOException;

/**
 * This class does parsing of True Type "hmtx" table and extracts all the data
 * needed.
 *
 * @author Sergey Shemyakov
 */
class TrueTypeHmtxTableParser extends TrueTypeTableParser {

    private int[] longHorMetrics;
    private int numberOfHMetrics;

    TrueTypeHmtxTableParser(InternalInputStream source, long offset) {
        super(source, offset);
    }

    void setNumberOfHMetrics(int numberOfHMetrics) {
        this.numberOfHMetrics = numberOfHMetrics;
    }

    @Override
    public void readTable() throws IOException {
        long startingOffset = this.source.getOffset();
        this.source.seek(this.offset);
        longHorMetrics = new int[numberOfHMetrics];
        for (int i = 0; i < numberOfHMetrics; ++i) {
            longHorMetrics[i] = this.readUFWord();
            this.source.skip(2);    // lsb
        }
        this.source.seek(startingOffset);
    }

    int[] getLongHorMetrics() {
        return longHorMetrics;
    }
}
