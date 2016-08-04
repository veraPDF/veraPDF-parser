package org.verapdf.font.truetype;

import org.verapdf.io.InternalInputStream;

import java.io.IOException;

/**
 * This class does parsing of True Type "cmap" table and extracts all the data
 * needed.
 *
 * @author Sergey Shemyakov
 */
class TrueTypeCmapTableParser extends TrueTypeTableParser {

    private int[] platformIDs;
    private int[] encodingIDs;

    TrueTypeCmapTableParser(InternalInputStream source, long offset) {
        super(source, offset);
    }

    @Override
    public void readTable() throws IOException {
        long startingOffset = this.source.getOffset();
        this.source.seek(this.offset);
        this.source.skip(2);    // version
        int numberOfTables = this.readUShort();
        this.platformIDs = new int[numberOfTables];
        this.encodingIDs = new int[numberOfTables];
        for (int i = 0; i < numberOfTables; ++i) {
            this.platformIDs[i] = this.readUShort();
            this.encodingIDs[i] = this.readUShort();
            this.source.skip(4);    // table offset
        }
        this.source.seek(startingOffset);
    }

    int[] getPlatformIDs() {
        return platformIDs;
    }

    int[] getEncodingIDs() {
        return encodingIDs;
    }
}
