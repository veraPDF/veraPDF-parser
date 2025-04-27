/**
 * This file is part of veraPDF Parser, a module of the veraPDF project.
 * Copyright (c) 2015-2025, veraPDF Consortium <info@verapdf.org>
 * All rights reserved.
 *
 * veraPDF Parser is free software: you can redistribute it and/or modify
 * it under the terms of either:
 *
 * The GNU General public license GPLv3+.
 * You should have received a copy of the GNU General Public License
 * along with veraPDF Parser as the LICENSE.GPL file in the root of the source
 * tree.  If not, see http://www.gnu.org/licenses/ or
 * https://www.gnu.org/licenses/gpl-3.0.en.html.
 *
 * The Mozilla Public License MPLv2+.
 * You should have received a copy of the Mozilla Public License along with
 * veraPDF Parser as the LICENSE.MPL file in the root of the source tree.
 * If a copy of the MPL was not distributed with this file, you can obtain one at
 * http://mozilla.org/MPL/2.0/.
 */
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
