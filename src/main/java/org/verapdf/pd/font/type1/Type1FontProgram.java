package org.verapdf.pd.font.type1;

import org.verapdf.as.io.ASInputStream;
import org.verapdf.cos.COSObject;
import org.verapdf.parser.COSParser;
import org.verapdf.parser.Token;
import org.verapdf.pd.font.FontProgram;
import org.verapdf.pd.font.truetype.TrueTypePredefined;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;

/**
 * This class does parsing of Type 1 font files.
 *
 * @author Sergey Shemyakov
 */
public class Type1FontProgram extends COSParser implements FontProgram {

    static final double[] DEFAULT_FONT_MATRIX = {0.001, 0, 0, 0.001, 0, 0};

    private double[] fontMatrix = DEFAULT_FONT_MATRIX;
    private String[] encoding;
    private Map<String, Integer> glyphWidths;
    private static final byte[] CLEAR_TO_MARK_BYTES =
            Type1StringConstants.CLEARTOMARK_STRING.getBytes();
    private boolean isFontParsed = false;

    /**
     * {@inheritDoc}
     */
    public Type1FontProgram(String fileName) throws IOException {
        super(fileName);
        encoding = new String[256];
    }

    /**
     * {@inheritDoc}
     */
    public Type1FontProgram(InputStream fileStream) throws IOException {
        super(fileStream);
        encoding = new String[256];
    }

    /**
     * This method is entry point for parsing process.
     *
     * @throws IOException if stream reading error occurs.
     */
    @Override
    public void parseFont() throws IOException {
        if (!isFontParsed) {
            isFontParsed = true;
            initializeToken();

            skipSpaces(true);

            while (getToken().type != Token.Type.TT_EOF) {
                nextToken();
                processToken();
            }
            if(glyphWidths == null) {
                throw new IOException("Type 1 font doesn't contain charstrings.");
            }
        }
    }

    private void processToken() throws IOException {
        switch (getToken().type) {
            case TT_NAME:
                switch (getToken().getValue()) {
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
                        if (isEncodingName()) {
                            break;
                        }
                        do {
                            nextToken();
                        } while (!this.getToken().getValue().equals(
                                Type1StringConstants.DUP_STRING) &&
                                this.getToken().type != Token.Type.TT_EOF);
                        if (this.getToken().type == Token.Type.TT_EOF) {
                            throw new IOException("Can't parse Type 1 font program");
                        }
                        this.source.unread(3);

                        while (true) {
                            nextToken();
                            if (this.getToken().getValue().equals(
                                    Type1StringConstants.READONLY_STRING)) {
                                break;
                            }
                            if (this.getToken().type == Token.Type.TT_EOF) {
                                throw new IOException("Can't parse Type 1 font program");
                            }
                            this.skipSpaces();
                            this.readNumber();
                            long key = this.getToken().integer;
                            this.nextToken();
                            encoding[(int) key] = this.getToken().getValue();
                            this.nextToken();
                        }
                        break;
                    default:
                        break;
                }
                break;
            case TT_KEYWORD:
                switch (getToken().getValue()) {
                    //Do processing of keywords like eexec
                    case Type1StringConstants.EEXEC_STRING:
                        this.skipSpaces();
                        long clearToMarkOffset = this.findOffsetCleartomark();
                        ASInputStream eexecEncoded = this.source.getStream(this.source.getOffset(),
                                clearToMarkOffset - this.source.getOffset());
                        ASInputStream eexecDecoded = new EexecFilterDecode(
                                eexecEncoded, false);
                        Type1PrivateParser parser = new Type1PrivateParser(
                                eexecDecoded, fontMatrix);
                        parser.parse();
                        this.glyphWidths = parser.getGlyphWidths();
                        this.source.seek(clearToMarkOffset);
                        break;
                }
                break;
            default:
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

    @Override
    public float getWidth(int charCode) {
        try {
            if (this.glyphWidths != null) {
                Integer res = this.glyphWidths.get(encoding[charCode]);
                if (res != null) {
                    return res;
                }
            }
            return -1;
        } catch (ArrayIndexOutOfBoundsException e) {
            return -1;
        }
    }

    @Override
    public float getWidth(String glyphName) {
        Integer res = this.glyphWidths.get(glyphName);
        return res == null ? -1 : res;
    }

    @Override
    public boolean containsCode(int code) {
        String glyphName = encoding[code];
        return this.glyphWidths != null &&
                this.glyphWidths.keySet().contains(glyphName);
    }

    public String[] getEncoding() {
        return encoding;
    }

    public String[] getCharSet() {
        Set<String> charSet = this.glyphWidths.keySet();
        return charSet.toArray(new String[charSet.size()]);
    }

    private boolean isEncodingName() throws IOException {
        long startOffset = this.source.getOffset();
        nextToken();
        String possibleEncodingName = getToken().getValue();
        nextToken();
        if (Type1StringConstants.DEF_STRING.equals(getToken().getValue())) {
            if (Type1StringConstants.STANDARD_ENCODING_STRING.equals(possibleEncodingName)) {
                this.encoding = TrueTypePredefined.STANDARD_ENCODING;
                this.source.seek(startOffset);
                return true;
            } else {
                throw new IOException("Can't get encoding " + possibleEncodingName + " as internal encoding of type 1 font program.");
            }
        }
        this.source.seek(startOffset);
        return false;
    }
}
