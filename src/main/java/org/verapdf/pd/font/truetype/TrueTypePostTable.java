package org.verapdf.pd.font.truetype;

import org.verapdf.io.SeekableInputStream;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * This class does parsing of True Type "post" table and extracts all the data
 * needed.
 *
 * @author Sergey Shemyakov
 */
class TrueTypePostTable extends TrueTypeTable {

    private long length;
    private int numGlyphs;
    private Map<String, Integer> stringToGid;

    TrueTypePostTable(SeekableInputStream source, long offset, long length) {
        super(source, offset);
        this.length = length;
        stringToGid = new HashMap<>();
    }

    void setNumGlyphs(int numGlyphs) {
        this.numGlyphs = numGlyphs;
    }

    @Override
    void readTable() throws IOException {
        long startingOffset = this.source.getOffset();
        this.source.seek(this.offset);
        float format = this.readFixed();
        this.source.skip(4 +             // italicAngle
                2 +             // underlinePosition
                2 +             // underlineThickness
                4 +             // isFixedPitch
                4 +             // minMemType42
                4 +             // maxMemType42
                4 +             // minMemType1
                4               // maxMemType1
        );
        if (format == 2.0f) {
            this.source.skip(2);    // numGlyphs
            int[] glyphNameIndexInt = new int[this.numGlyphs];
            for (int i = 0; i < this.numGlyphs; ++i) {
                glyphNameIndexInt[i] = this.readUShort();
            }
            List<String> strings = new LinkedList<>();
            while (this.source.getOffset() < this.offset + this.length) {
                strings.add(this.readPascalString());
            }
            for (int i = 0; i < this.numGlyphs; ++i) {
                if (glyphNameIndexInt[i] < TrueTypePredefined.MAC_INDEX_TO_GLYPH_NAME.length) {
                    stringToGid.put(TrueTypePredefined.MAC_INDEX_TO_GLYPH_NAME[glyphNameIndexInt[i]], i);
                } else {
                    stringToGid.put(strings.get(glyphNameIndexInt[i] -
                            TrueTypePredefined.MAC_INDEX_TO_GLYPH_NAME.length), i);
                }
            }
        } else if (format == 2.5f) {
            try {
                for (int i = 0; i < this.numGlyphs; ++i) {
                    int tableOffset = this.readChar();
                    String glyphName =
                            TrueTypePredefined.MAC_INDEX_TO_GLYPH_NAME[tableOffset + i];
                    this.stringToGid.put(glyphName, i);
                }
            } catch (ArrayIndexOutOfBoundsException e) {
                throw new IOException("Error in reading post table", e);
            }
        }
        this.source.seek(startingOffset);
    }

    int getGID(String s) {
        Integer res = this.stringToGid.get(s);
        return res == null ? 0 : res.intValue();    // gid for .notdef
    }

    private String readPascalString() throws IOException {
        int length = this.readByte();
        byte[] str = new byte[length];
        this.source.read(str, length);
        return new String(str);
    }
}
