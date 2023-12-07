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
package org.verapdf.pd.font;

import org.verapdf.tools.resource.ASFileStreamCloser;

import java.io.IOException;
import java.util.List;

/**
 * Interface for all fonts in pdflib.
 *
 * @author Sergey Shemyakov
 */
public interface FontProgram {

    /**
     * Returns width of glyph for given character code.
     *
     * @param code is code for glyph.
     * @return width of corresponding glyph or -1 if glyph is not found.
     */
    float getWidth(int code);

    /**
     * Returns width of glyph for given glyph name.
     *
     * @param glyphName is name for glyph.
     * @return width of corresponding glyph or -1 if glyph is not found.
     */
    float getWidth(String glyphName);

    /**
     * This method does parsing of font, after it all the data needed should be
     * extracted.
     * @throws IOException if error in font parsing occurs.
     */
    void parseFont() throws IOException;

    /**
     * Checks if font contains character with given code.
     *
     * @param code is character code.
     * @return true if font contains character with given code.
     */
    boolean containsCode(int code);

    /**
     * Method works only for fonts where cid notation is used.
     * @return true if font a is cid font and contains given cid.
     */
    boolean containsCID(int cid);

    /**
     * Checks if this font program has glyph for given glyph name.
     *
     * @param glyphName is the name of glyph.
     * @return true if this font program has glyph for given name.
     */
    boolean containsGlyph(String glyphName);

    /**
     * @return true if font parsing has been attempted.
     */
    boolean isAttemptedParsing();

    /**
     * @return true if font was successfully parsed.
     */
    boolean isSuccessfulParsing();

    /**
     * Returns glyph name for glyph with given code.
     *
     * @param code is code of glyph.
     * @return glyph name.
     */
    String getGlyphName(int code);

    /**
     * @return file stream closer that handles the closing of font program
     * stream or null if stream is memory stream.
     */
    ASFileStreamCloser getFontProgramResource();

    String getWeight();

    Double getAscent();

    Double getDescent();

    List<Integer> getCIDList();
}
