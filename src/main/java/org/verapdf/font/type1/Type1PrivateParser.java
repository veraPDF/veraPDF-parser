package org.verapdf.font.type1;

import org.verapdf.as.io.ASInputStream;
import org.verapdf.parser.BaseParser;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * This class parses private data in font Type 1 files after it was
 * eexec-decoded. In particular, it extracts glyph info.
 *
 * @author Sergey Shemyakov
 */
class Type1PrivateParser extends BaseParser {

    private int lenIV;
    private Map<String, Integer> glyphWidths;
    private double[] fontMatrix;

    /**
     * {@inheritDoc}
     */
    public Type1PrivateParser(ASInputStream stream, double[] fontMatrix) throws IOException {
        super(stream);
        glyphWidths = new HashMap<>();
        this.fontMatrix = fontMatrix;
    }

    public void parse() throws IOException {
        initializeToken();
        do {
            nextToken();
        } while (this.getToken().token.equals("CharStrings"));
        nextToken();
        int amountOfGlyphs = (int) this.getToken().integer;
        nextToken();    // reading "dict"
        nextToken();    // reading "dup"
        nextToken();    // reading "begin"
        for(int i = 0; i < amountOfGlyphs; ++i) {
            readCharString();
        }
    }

    /**
     * @return an integer specifying the number of random bytes at the beginning
     * of charstrings for charstring encryption
     */
    public int getLenIV() {
        return lenIV;
    }

    private void readCharString() {

    }
}
