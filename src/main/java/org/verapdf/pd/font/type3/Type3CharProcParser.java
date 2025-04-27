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
package org.verapdf.pd.font.type3;

import org.verapdf.as.io.ASInputStream;
import org.verapdf.parser.NotSeekableBaseParser;
import org.verapdf.parser.Token;

import java.io.IOException;

/**
 * Parses type 3 char procs to obtain glyph widths.
 *
 * @author Sergey Shemyakov
 */
public class Type3CharProcParser extends NotSeekableBaseParser {

    private double width = -1;
    private static final String D0 = "d0";
    private static final String D1 = "d1";

    /**
     * Constructor parser from char proc data.
     */
    public Type3CharProcParser(ASInputStream charProcStream) throws IOException {
        super(charProcStream);
    }

    /**
     * Parses width from given char proc string.
     *
     * @throws IOException if stream reading error occurred or input stream can't
     *                     be parsed.
     */
    public void parse() throws IOException {
        this.initializeToken();
        nextToken();    // w_x
        if (getToken().type == Token.Type.TT_INTEGER || getToken().type == Token.Type.TT_REAL) {
            this.width = getToken().real;
        }

        nextToken();    // w_y
        nextToken();
        if (getToken().type == Token.Type.TT_KEYWORD && getToken().getValue().equals(D0)) {
            return;
        }   // else ll_x

        nextToken();    // ll_y
        nextToken();    // ur_x
        nextToken();    // ur_y
        nextToken();    // d1

        if (getToken().type != Token.Type.TT_KEYWORD || !getToken().getValue().equals(D1)) {    // stream is corrupted
            this.width = -1;
            throw new IOException("Can't parse type 3 char proc");
        }
    }

    /**
     * @return width of glyph presented by given char proc or -1 if parsing
     * failed or was not performed.
     */
    public double getWidth() {
        return width;
    }
}
