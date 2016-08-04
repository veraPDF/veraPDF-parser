package org.verapdf.font.truetype;

import org.verapdf.io.InternalInputStream;

import java.io.IOException;

/**
 * This is base class for all True Type table parsers.
 *
 * @author Sergey Shemyakov
 */
abstract class TrueTypeTableParser extends TrueTypeBaseParser {

    protected long offset;

    protected TrueTypeTableParser(InternalInputStream source, long offset) {
        super(source);
        this.offset = offset;
    }

    /**
     * This method extracts all the data needed from table.
     *
     * @throws IOException if stream-reading error occurs.
     */
    public abstract void readTable() throws IOException;
}
