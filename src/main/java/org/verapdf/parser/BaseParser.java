package org.verapdf.parser;

import org.verapdf.as.CharTable;
import org.verapdf.as.io.ASInputStream;
import org.verapdf.cos.filters.COSFilterASCII85Decode;
import org.verapdf.cos.filters.COSFilterASCIIHexDecode;

import java.io.IOException;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Maxim Plushchov
 */
public abstract class BaseParser {

    private static final Logger LOGGER = Logger.getLogger(BaseParser.class.getCanonicalName());

    // max string length in bytes
    private static final int MAX_STRING_LENGTH = 65535;
    private static final byte ASCII_ZERO = 48;
    private static final byte ASCII_NINE = 57;
    
    // indicates if this parser is a postscript parser
    protected boolean isPSParser = false;

    protected Token token;

    protected BaseParserInputStream source;

    public Token getToken() {
        return this.token;
    }

    public void initializeToken() {
        if (this.token == null) {
            this.token = new Token();
        }
    }

    protected void appendToToken(final int ch, final boolean append) {
        if (append) {
            appendToToken(ch);
        }
    }

    protected void appendToToken(final int ch) {
        this.token.append(ch);
    }

    protected void clearToken() {
        this.token.clearValue();
    }

    protected String readUntilDelimiter() throws IOException {
        initializeToken();
        this.token.clearValue();
        byte ch = this.source.readByte();
        while (!CharTable.isSpace(ch) && !CharTable.isTokenDelimiter(ch)) {
            appendToToken(ch);
            if (this.source.isEOF()) {
                break;
            } else {
                ch = this.source.readByte();
            }
        }
        if (CharTable.isSpace(ch) || CharTable.isTokenDelimiter(ch)) {
            this.source.unread();
        }
        return this.token.getValue();
    }

    protected boolean findKeyword(final Token.Keyword keyword) throws IOException {
        nextToken();
        while (this.token.type != Token.Type.TT_EOF && (this.token.type != Token.Type.TT_KEYWORD || this.token.keyword != keyword)) {
            nextToken();
        }
        return this.token.type == Token.Type.TT_KEYWORD && this.token.keyword == keyword;
    }

    protected abstract boolean findKeyword(final Token.Keyword keyword, final int lookUpSize) throws IOException;

    protected void skipSpaces() throws IOException {
        this.skipSpaces(false);
    }

    public void skipSpaces(boolean skipComment) throws IOException {
        while (skipSingleSpace(skipComment));
    }

    protected boolean skipSingleSpace(boolean skipComment) throws IOException {
        if (this.source.isEOF()) {
            return false;
        }
        byte ch = this.source.readByte();
        if (CharTable.isSpace(ch)) {
            return true;
        }
        if (ch == '%' && skipComment) {
            skipComment();
            return true;
        }
        this.source.unread();
        return false;
    }

    protected abstract void skipComment() throws IOException;

    protected boolean isDigit() throws IOException {
        return isDigit((byte) this.source.peek());
    }

    protected static boolean isDigit(byte c) {
        return c >= ASCII_ZERO && c <= ASCII_NINE;
    }

    protected static boolean isLF(int c) {
        return CharTable.ASCII_LF == c;
    }

    public static boolean isCR(int c) {
        return CharTable.ASCII_CR == c;
    }

    public static boolean isFF(int c) {
        return CharTable.ASCII_FF == c;
    }

    protected abstract void readASCII85() throws IOException;
    
    protected void decodeASCII85(ASInputStream ascii85, int length) throws IOException {
        try (COSFilterASCII85Decode ascii85Decode = new COSFilterASCII85Decode(ascii85)) {
            byte[] buf = new byte[length];
            int read = ascii85Decode.read(buf);

            this.token.setContainsOnlyHex(false);
            this.token.setHexCount(0L);
            if (read == -1) {
                LOGGER.log(Level.WARNING, "Invalid ASCII85 string");
                this.token.clearValue();
            } else {
                this.token.setByteValue(Arrays.copyOf(buf, read));
            }
        }
    }

    private void readToken() throws IOException {
        this.token.clearValue();
        byte ch;
        while (!this.source.isEOF()) {
            ch = this.source.readByte();
            if (CharTable.isTokenDelimiter(ch)) {
                this.source.unread();
                break;
            }

            appendToToken(ch);
        }
    }

    public void nextToken() throws IOException {
        skipSpaces(true);
        if (this.source.isEOF()) {
            this.token.type = Token.Type.TT_EOF;
            return;
        }

        this.token.type = Token.Type.TT_NONE;

        byte ch = this.source.readByte();

        switch (ch) {
            case '(':
                this.token.type = Token.Type.TT_LITSTRING;
                readLitString();
                break;
            case ')':
                //error
                break;
            case '<':
                ch = source.readByte();
                if (ch == '<') {
                    this.token.type = Token.Type.TT_OPENDICT;
                } else if (ch == '~') {
                    this.token.type = Token.Type.TT_HEXSTRING;
                    readASCII85();
                } else {
                    this.source.unread();
                    this.token.type = Token.Type.TT_HEXSTRING;
                    readHexString();
                }
                break;
            case '>':
                ch = this.source.readByte();
                if (ch == '>') {
                    this.token.type = Token.Type.TT_CLOSEDICT;
                } else {
                    throw new IOException(getErrorMessage("Unknown symbol " + ch + " after \'>\'"));
                }
                break;
            case '[':
                this.token.type = Token.Type.TT_OPENARRAY;
                break;
            case ']':
                this.token.type = Token.Type.TT_CLOSEARRAY;
                break;
            case '{': // as delimiter in PostScript calculator functions 181
                if (isPSParser) {
                    this.token.type = Token.Type.TT_STARTPROC;
                }
                break;
            case '}':
                if (isPSParser) {
                    this.token.type = Token.Type.TT_ENDPROC;
                }
                break;
            case '/':
                this.token.type = Token.Type.TT_NAME;
                readName();
                break;
            case '0':
            case '1':
            case '2':
            case '3':
            case '4':
            case '5':
            case '6':
            case '7':
            case '8':
            case '9':
            case '.':
                this.source.unread();
                readNumber();
                break;
            case '+':
                readNumber();
                break;
            case '-':
                readNumber();
                this.token.integer = -this.token.integer;
                this.token.real = -this.token.real;
                break;
            default:
                this.source.unread();
                readToken();
                this.token.toKeyword();
                if (this.token.keyword == Token.Keyword.KW_NONE) {
                    this.token.type = Token.Type.TT_NONE;
                }
                break;
        }
    }

    protected String getLine() throws IOException {
        initializeToken();
        this.token.clearValue();
        byte ch = this.source.readByte();
        while (!this.source.isEOF()) {
            if (ch == CharTable.ASCII_LF || ch == CharTable.ASCII_CR) {
                break;
            }
            appendToToken(ch);
            ch = this.source.readByte();
        }
        return this.token.getValue();
    }

    protected byte[] getLineBytes() throws IOException {
        getLine();
        return this.token.getByteValue();
    }

    private void readHexString() throws IOException {
        this.token.clearValue();
        byte ch;
        int uc = 0;
        int hex;

        //these are required for pdf/a validation
        boolean containsOnlyHex = true;
        long hexCount = 0;

        boolean odd = false;
        while (!this.source.isEOF()) {
            ch = this.source.readByte();
            if (ch == '>') {
                if (odd) {
                    uc <<= 4;
                    appendToToken(uc);
                }
                this.token.setContainsOnlyHex(containsOnlyHex);
                this.token.setHexCount(hexCount);
                return;
            } else if (!CharTable.isSpace(ch)) {
                hex = COSFilterASCIIHexDecode.decodeLoHex(ch);
                hexCount++;
                if (hex < 16 && hex > -1) { // skip all non-Hex characters
                    if (odd) {
                        uc = (uc << 4) + hex;
                        appendToToken(uc);
                        uc = 0;
                    } else {
                        uc = hex;
                    }
                    odd = !odd;
                } else {
                    containsOnlyHex = false;
                }
            }
        }

        this.token.setContainsOnlyHex(containsOnlyHex);
        this.token.setHexCount(hexCount);
    }

    protected void readNumber() throws IOException {
        try {
            int radix = 10;
            initializeToken();
            this.token.clearValue();
            this.token.type = Token.Type.TT_INTEGER;
            byte ch;
            while (!this.source.isEOF()) {
                ch = this.source.readByte();
                if (CharTable.isTokenDelimiter(ch)) {
                    this.source.unread();
                    break;
                }
                if (ch >= '0' && ch <= '9') {
                    appendToToken(ch);
                } else if (ch == '.') {
                    this.token.type = Token.Type.TT_REAL;
                    appendToToken(ch);
                } else if (ch == '#' && isPSParser) {
                    if (this.token.type == Token.Type.TT_INTEGER) {
                        radix = Integer.parseInt(this.token.getValue());
                    }
                    token.clearValue();
                } else {
                    this.source.unread();
                    break;
                }
            }
            if (this.token.type == Token.Type.TT_INTEGER) {
                long value = Long.valueOf(this.token.getValue(), radix);
                this.token.integer = value;
                this.token.real = value;
            } else {
                double value = Double.parseDouble(this.token.getValue());
                this.token.integer = Math.round(value);
                this.token.real = value;
            }
        } catch (NumberFormatException e) {
            LOGGER.log(Level.FINE, getErrorMessage(""), e);
            this.token.integer = Math.round(Double.MAX_VALUE);
            this.token.real = Double.MAX_VALUE;
        }
    }

    private void readLitString() throws IOException {
        this.token.clearValue();

        int parenthesesDepth = 0;
        
        boolean append = true;

        byte ch = this.source.readByte();
        while (!this.source.isEOF()) {
            switch (ch) {
                default:
                    appendToToken(ch, append);
                    break;
                case '(':
                    parenthesesDepth++;
                    appendToToken(ch, append);
                    break;
                case ')':
                    if (parenthesesDepth == 0) {
                        return;
                    }

                    parenthesesDepth--;
                    appendToToken(ch, append);
                    break;
                case '\\': {
                    ch = this.source.readByte();
                    switch (ch) {
                        case '(':
                            appendToToken(CharTable.ASCII_LEFT_PAR, append);
                            break;
                        case ')':
                            appendToToken(CharTable.ASCII_RIGHT_PAR, append);
                            break;
                        case 'n':
                            appendToToken(CharTable.ASCII_LF, append);
                            break;
                        case 'r':
                            appendToToken(CharTable.ASCII_CR, append);
                            break;
                        case 't':
                            appendToToken(CharTable.ASCII_HT, append);
                            break;
                        case 'b':
                            appendToToken(CharTable.ASCII_BS, append);
                            break;
                        case 'f':
                            appendToToken(CharTable.ASCII_FF, append);
                            break;
                        case '0':
                        case '1':
                        case '2':
                        case '3':
                        case '4':
                        case '5':
                        case '6':
                        case '7': {
                            if (!isPSParser) {
                                // look for 1, 2, or 3 octal characters
                                char ch1 = (char) (ch - '0');
                                for (int i = 1; i < 3; i++) {
                                    ch = this.source.readByte();
                                    if (ch < '0' || ch > '7') {
                                        this.source.unread();
                                        break;
                                    }
                                    ch1 = (char) ((ch1 << 3) + (ch - '0'));
                                }
                                appendToToken(ch1, append);
                            }
                            break;
                        }
                        case CharTable.ASCII_LF:
                            break;
                        case CharTable.ASCII_CR:
                            ch = this.source.readByte();
                            if (ch != CharTable.ASCII_LF) {
                                this.source.unread();
                            }
                            break;
                        default:
                            appendToToken(ch, append);
                            break;
                    }
                    break;
                }
            }
            ch = source.readByte();
            if (append && token.getSize() > MAX_STRING_LENGTH) {
                LOGGER.log(Level.WARNING, getErrorMessage("Content stream string token exceeds " + MAX_STRING_LENGTH + " bytes"));
                append = false;
            }
        }
    }

    protected void readName() throws IOException {
        this.token.clearValue();
        byte ch;
        while (!this.source.isEOF()) {
            ch = this.source.readByte();
            if (CharTable.isTokenDelimiter(ch)) {
                this.source.unread();
                break;
            }

            // if ch == # (0x23)
            if (ch == '#' && !isPSParser) {
                byte ch1;
                byte ch2;
                byte dc;
                ch1 = this.source.readByte();
                if (!source.isEOF() && COSFilterASCIIHexDecode.decodeLoHex(ch1) != COSFilterASCIIHexDecode.ER) {
                    dc = COSFilterASCIIHexDecode.decodeLoHex(ch1);
                    ch2 = this.source.readByte();
                    if (!this.source.isEOF() && COSFilterASCIIHexDecode.decodeLoHex(ch2) != COSFilterASCIIHexDecode.ER) {
                        dc = (byte) ((dc << 4) + COSFilterASCIIHexDecode.decodeLoHex(ch2));
                        appendToToken(dc);
                    } else {
                        appendToToken(ch);
                        appendToToken(ch1);
                        this.source.unread();
                    }
                } else {
                    appendToToken(ch);
                    this.source.unread();
                }
            } else {
                appendToToken(ch);
            }
        }
    }
    
    protected String getErrorMessage(String message) {
        return message;
    }

    protected BaseParserInputStream getSource() {
        return source;
    }
    
    protected boolean isPSParser() {
        return isPSParser;
    }
}
