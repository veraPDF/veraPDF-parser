package org.verapdf.font.type1;

import org.verapdf.as.io.ASFileInStream;
import org.verapdf.as.io.ASInputStream;
import org.verapdf.cos.COSObject;
import org.verapdf.cos.filters.COSFilterASCIIHexDecode;
import org.verapdf.parser.COSParser;
import org.verapdf.parser.Token;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Map;

/**
 * This class does parsing of Type 1 font files.
 *
 * @author Sergey Shemyakov
 */
public class Type1Parser extends COSParser {

    static final double[] DEFAULT_FONT_MATRIX = {0.001, 0, 0, 0.001, 0, 0};

    private double[] fontMatrix = DEFAULT_FONT_MATRIX;
    private String[] encoding;
    private Map<String, Integer> glyphWidths;
    private static final byte[] CLEAR_TO_MARK_BYTES =
            Type1StringConstants.CLEARTOMARK_STRING.getBytes();

    /**
     * {@inheritDoc}
     */
    public Type1Parser(String fileName) throws IOException {
        super(fileName);
        encoding = new String[256];
    }

    /**
     * {@inheritDoc}
     */
    public Type1Parser(InputStream fileStream) throws IOException {
        super(fileStream);
        encoding = new String[256];
    }

    /**
     * {@inheritDoc}
     */
    public Type1Parser(ASInputStream asInputStream) throws IOException {
        super(asInputStream);
        encoding = new String[256];
    }

    /**
     * This method is entry point for parsing process.
     *
     * @throws IOException if stream reading error occurs.
     */
    public void parse() throws IOException {
        initializeToken();

        skipSpaces(true);

        while (getToken().type != Token.Type.TT_EOF) {
            nextToken();
            processToken();
        }
    }

    private void processToken() throws IOException {
        switch (getToken().type) {
            case TT_NAME:
                switch (getToken().token) {
                    //Do processing of all necessary names like /FontName, /FamilyName, etc.
                    case Type1StringConstants.FONT_MATRIX_STRING:
                        this.skipSpaces();
                        this.nextToken();
                        if (this.getToken().type == Token.Type.TT_OPENARRAY) {
                            this.source.unread();
                            COSObject cosFontMatrix = this.nextObject();
                            if (cosFontMatrix.size() == 6) {
                                for (int i = 0; i < 6; ++i) {
                                    fontMatrix[i] = cosFontMatrix.at(i).getReal();
                                }
                            }
                        }
                        break;
                    case Type1StringConstants.ENCODING_STRING:
                        do {
                            nextToken();
                        } while (!this.getToken().token.equals(
                                Type1StringConstants.DUP_STRING));
                        this.source.unread(3);

                        while (true) {
                            nextToken();
                            if (this.getToken().token.equals(
                                    Type1StringConstants.READONLY_STRING)) {
                                break;
                            }
                            this.skipSpaces();
                            this.readNumber();
                            long key = this.getToken().integer;
                            this.nextToken();
                            encoding[(int) key] = this.getToken().token;
                            this.nextToken();
                        }
                        break;
                }
                break;
            case TT_NONE:
                switch (getToken().token) {
                    //Do processing of keywords like eexec
                    case Type1StringConstants.EEXEC_STRING:
                        this.skipSpaces();
                        long clearToMarkOffset = this.findOffsetCleartomark();
                        ASFileInStream eexecEncoded = new ASFileInStream(
                                this.source.getStream(), this.source.getOffset(),
                                clearToMarkOffset - this.source.getOffset());
                        ASInputStream eexecDecoded = new EexecFilterDecode(
                                new COSFilterASCIIHexDecode(eexecEncoded), false);
                        Type1PrivateParser parser = new Type1PrivateParser(
                                eexecDecoded, fontMatrix);
                        parser.parse();
                        this.glyphWidths = parser.getGlyphWidths();
                        this.source.seek(clearToMarkOffset);
                        break;
                }
                break;
        }
    }

    private long findOffsetCleartomark() throws IOException {
        long startingOffset = this.source.getOffset();
        int length = CLEAR_TO_MARK_BYTES.length;
        this.source.seek(this.source.getStreamLength() - length);
        byte[] buf = new byte[length];
        this.source.read(buf, length);
        while (!Arrays.equals(buf, CLEAR_TO_MARK_BYTES)) {
            this.source.unread(length + 1);
            this.source.read(buf, length);
        }
        long res = this.source.getOffset() - length;
        this.source.seek(startingOffset);
        return res - 512;
    }

}
