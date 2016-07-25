package org.verapdf.font.type1;

import org.verapdf.as.filters.io.ASBufferingInFilter;
import org.verapdf.as.io.ASFileInStream;
import org.verapdf.as.io.ASInputStream;
import org.verapdf.parser.BaseParser;
import org.verapdf.parser.Token;

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
    private Map<String, Double> glyphWidths;
    private double[] fontMatrix;

    /**
     * {@inheritDoc}
     */
    public Type1PrivateParser(ASInputStream stream, double[] fontMatrix) throws IOException {
        super(stream);
        glyphWidths = new HashMap<>();
        this.fontMatrix = fontMatrix;
        this.lenIV = 4;
    }

    public void parse() throws IOException {
        initializeToken();

        skipSpaces(true);

        while (getToken().type != Token.Type.TT_EOF) {
            nextToken();
            processToken();
        }
    }

    private void processToken() throws IOException {
        switch (this.getToken().type) {
            case TT_NAME:
                switch (this.getToken().token) {
                    case "CharStrings":
                        nextToken();
                        int amountOfGlyphs = (int) this.getToken().integer;
                        nextToken();    // reading "dict"
                        nextToken();    // reading "dup"
                        nextToken();    // reading "begin"
                        for (int i = 0; i < amountOfGlyphs; ++i) {
                            decodeCharString();
                        }
                        break;
                    case "LenIV":
                        this.nextToken();
                        if (this.getToken().type == Token.Type.TT_INTEGER) {
                            this.lenIV = (int) this.getToken().integer;
                        }
                        break;
                }
        }
    }

    /**
     * @return an integer specifying the number of random bytes at the beginning
     * of charstrings for charstring encryption
     */
    public int getLenIV() {
        return lenIV;
    }

    private void decodeCharString() throws IOException {
        this.nextToken();
        checkTokenType(Token.Type.TT_NAME);
        String glyphName = this.getToken().token;
        this.nextToken();
        checkTokenType(Token.Type.TT_INTEGER);
        long charstringLength = this.getToken().integer;
        this.nextToken();
        this.skipSpaces();
        long beginOffset = this.source.getOffset();
        this.source.skip((int) charstringLength);
        ASBufferingInFilter charString = new ASBufferingInFilter(
                new ASFileInStream(this.source.getStream(), beginOffset, charstringLength));
        ASInputStream decodedCharString = new EexecFilterDecode(
                charString, true, this.getLenIV());
        CharStringParser parser = new CharStringParser(decodedCharString);
        glyphWidths.put(glyphName, applyFontMatrix(parser.getWidth()));
        this.nextToken();
    }

    private void checkTokenType(Token.Type expectedType) throws IOException {
        if (this.getToken().type != expectedType) {
            throw new IOException("Error in parsing Private dictionary of font 1" +
                    " file, expected type " + expectedType + ", but got " + this.getToken().type);
        }
    }

    private double applyFontMatrix(int width) {
        return width * fontMatrix[0] + fontMatrix[4];
    }

    Map<String, Double> getGlyphWidths() {
        return glyphWidths;
    }
}
