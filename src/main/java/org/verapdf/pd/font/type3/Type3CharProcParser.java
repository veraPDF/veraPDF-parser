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
