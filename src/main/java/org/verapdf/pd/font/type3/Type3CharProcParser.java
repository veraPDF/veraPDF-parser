/*
 * This file is part of veraPDF Parser, a module of the veraPDF project.
 * Copyright (c) 2015-2026, veraPDF Consortium <info@verapdf.org>
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
    private Double ascent;
    private Double descent;
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
        if (getToken().type == Token.Type.TT_INTEGER || getToken().type == Token.Type.TT_REAL) {
            this.descent = getToken().real;
        }
        nextToken();    // ur_x
        nextToken();    // ur_y
        if (getToken().type == Token.Type.TT_INTEGER || getToken().type == Token.Type.TT_REAL) {
            this.ascent = getToken().real;
        }
        nextToken();    // d1

        if (getToken().type != Token.Type.TT_KEYWORD || !getToken().getValue().equals(D1)) {    // stream is corrupted
            this.width = -1;
            this.ascent = null;
            this.descent = null;
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

    /**
     * @return ascent of the glyph presented by given char proc, or 0 if the char
     * proc declares no glyph bounding box.
     * @deprecated a char proc that begins with the d0 operator specifies the
     * glyph width only (ISO 32000-1, 9.6.5.3) and declares no glyph bounding
     * box, so its ascent is undefined rather than 0. This method cannot express
     * that; use {@link #getAscentOrNull()} instead.
     */
    @Deprecated
    public double getAscent() {
        return ascent == null ? 0 : ascent;
    }

    /**
     * @return descent of the glyph presented by given char proc, or 0 if the
     * char proc declares no glyph bounding box.
     * @deprecated undefined descent cannot be distinguished from 0; use
     * {@link #getDescentOrNull()} instead. See {@link #getAscent()}.
     */
    @Deprecated
    public double getDescent() {
        return descent == null ? 0 : descent;
    }

    /**
     * @return ascent of the glyph presented by given char proc, or null if the
     * char proc declares no glyph bounding box. A char proc that begins with
     * the d0 operator specifies the glyph width only (ISO 32000-1, 9.6.5.3),
     * so its ascent is undefined rather than 0.
     */
    public Double getAscentOrNull() {
        return ascent;
    }

    /**
     * @return descent of the glyph presented by given char proc, or null if the
     * char proc declares no glyph bounding box. See {@link #getAscentOrNull()}.
     */
    public Double getDescentOrNull() {
        return descent;
    }
}
