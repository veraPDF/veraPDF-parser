/**
 * This file is part of veraPDF Parser, a module of the veraPDF project.
 * Copyright (c) 2015, veraPDF Consortium <info@verapdf.org>
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
 * This class does parsing of True Type "hmtx" table and extracts all the data
 * needed.
 *
 * @author Sergey Shemyakov
 */
class TrueTypeHmtxTable extends TrueTypeTable {

    private int[] longHorMetrics;
    private int numberOfHMetrics;

    TrueTypeHmtxTable(SeekableInputStream source, long offset) {
        super(source, offset);
    }

    void setNumberOfHMetrics(int numberOfHMetrics) {
        this.numberOfHMetrics = numberOfHMetrics;
    }

    @Override
    void readTable() throws IOException {
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
