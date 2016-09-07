package org.verapdf.pd.font.type1;

import org.verapdf.as.filters.io.ASBufferingInFilter;
import org.verapdf.as.io.ASFileInStream;
import org.verapdf.as.io.ASInputStream;
import org.verapdf.parser.BaseParser;
import org.verapdf.parser.Token;

import java.io.IOException;
import java.util.Arrays;
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
    private boolean isDefaultFontMatrix;

    /**
     * {@inheritDoc}
     */
    public Type1PrivateParser(ASInputStream stream, double[] fontMatrix) throws IOException {
        super(stream);
        glyphWidths = new HashMap<>();
        this.fontMatrix = fontMatrix;
        isDefaultFontMatrix = Arrays.equals(this.fontMatrix,
                Type1FontProgram.DEFAULT_FONT_MATRIX);
        this.lenIV = 4;
    }

    public void parse() throws IOException {
        initializeToken();

        skipSpaces(true);

        while (getToken().type != Token.Type.TT_EOF &&
                !Type1StringConstants.CLOSEFILE.equals(getToken().getValue())) {
            nextToken();
            processToken();
        }
    }

    private void processToken() throws IOException {
        switch (this.getToken().type) {
            case TT_NAME:
                switch (this.getToken().getValue()) {
                    case Type1StringConstants.CHAR_STRINGS_STRING:
                        nextToken();
                        int amountOfGlyphs = (int) this.getToken().integer;
                        nextToken();    // reading "dict"
                        nextToken();    // reading "dup"
                        nextToken();    // reading "begin"
                        for (int i = 0; i < amountOfGlyphs; ++i) {
                            decodeCharString();
                        }
                        break;
                    case Type1StringConstants.LEN_IV_STRING:
                        this.nextToken();
                        if (this.getToken().type == Token.Type.TT_INTEGER) {
                            this.lenIV = (int) this.getToken().integer;
                        }
                        break;
                    case Type1StringConstants.SUBRS:    // skipping binary data that can be bad for parser
                        nextToken();
                        int amountOfSubrs = (int) this.getToken().integer;
                        nextToken();    // reading "array"
                        for (int i = 0; i < amountOfSubrs; ++i) {
                            nextToken();    // reading "dup"
                            nextToken();    // reading number
                            nextToken();
                            long toSkip = this.getToken().integer;
                            nextToken();    // reading "RD"
                            this.skipSpaces();
                            this.source.skip(toSkip);
                            this.nextToken();   // reading "NP"
                        }
                        break;
                    default:
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
        String glyphName = this.getToken().getValue();
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
        Type1CharStringParser parser = new Type1CharStringParser(decodedCharString);
        if (!isDefaultFontMatrix) {
            glyphWidths.put(glyphName, applyFontMatrix(parser.getWidth().getInteger()));
        } else {
            glyphWidths.put(glyphName, (int) parser.getWidth().getInteger());
        }
        this.nextToken();
    }

    private void checkTokenType(Token.Type expectedType) throws IOException {
        if (this.getToken().type != expectedType) {
            throw new IOException("Error in parsing Private dictionary of font 1" +
                    " file, expected type " + expectedType + ", but got " + this.getToken().type);
        }
    }

    private int applyFontMatrix(long width) {
        return (int) (width * fontMatrix[0]) * 1000;
    }

    Map<String, Integer> getGlyphWidths() {
        return glyphWidths;
    }
}
