package org.verapdf.pd.font;

import org.verapdf.tools.resource.ASFileStreamCloser;

import java.io.IOException;

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
     * Checks if font contains character with given ID.
     *
     * @param code is character code.
     * @return true if font contains character with given ID.
     */
    boolean containsCode(int code);

    /**
     * @return true if font parsing has been attempted.
     */
    boolean isAttemptedParsing();

    /**
     * @return true if font was successfully parsed.
     */
    boolean isSuccessfulParsing();

    /**
     * @return file stream closer that handles the closing of font program
     * stream or null if stream is memory stream.
     */
    ASFileStreamCloser getFontProgramResource();
}
