/**
 * This file is part of veraPDF Parser, a module of the veraPDF project.
 * Copyright (c) 2015-2024, veraPDF Consortium <info@verapdf.org>
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
 * This is base class for all True Type table parsers.
 *
 * @author Sergey Shemyakov
 */
abstract class TrueTypeTable extends TrueTypeBaseParser {

    protected long offset;

    protected TrueTypeTable(SeekableInputStream source, long offset) {
        super(source);
        this.offset = offset;
    }

    /**
     * Empty constructor for inherited classes. Should be used to set Table
     * values to default if table is not present in font program.
     */
    protected TrueTypeTable() {}

    /**
     * This method extracts all the data needed from table.
     *
     * @throws IOException if stream-reading error occurs.
     */
    abstract void readTable() throws IOException;
}
