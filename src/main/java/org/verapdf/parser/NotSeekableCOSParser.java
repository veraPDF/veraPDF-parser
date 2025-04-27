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
package org.verapdf.parser;

import org.verapdf.as.filters.io.ASBufferedInFilter;
import org.verapdf.as.io.ASInputStream;

import java.io.Closeable;
import java.io.IOException;

/**
 * Parser for COS objects that operates with a buffered stream. The seek()
 * operation of stream is not required.
 *
 * @author Sergey Shemyakov
 */
public class NotSeekableCOSParser extends COSParser implements Closeable {

    public NotSeekableCOSParser(ASInputStream stream) throws IOException {
        super(new NotSeekableBaseParser(stream));
    }

    public NotSeekableCOSParser(ASInputStream stream, boolean isPSParser) throws IOException {
        super(new NotSeekableBaseParser(stream, isPSParser));
    }

    public NotSeekableCOSParser(NotSeekableBaseParser baseParser) {
        super(baseParser);
    }

    @Override
    public NotSeekableBaseParser getBaseParser() {
        return (NotSeekableBaseParser) super.getBaseParser();
    }

    @Override
    public ASBufferedInFilter getSource() {
        return getBaseParser().getSource();
    }

    @Override
    public void close() throws IOException {
        getBaseParser().close();
    }
}
