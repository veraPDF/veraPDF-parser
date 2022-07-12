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
package org.verapdf.parser;

import org.verapdf.as.CharTable;
import org.verapdf.as.filters.io.ASBufferedInFilter;
import org.verapdf.as.io.ASInputStream;
import org.verapdf.as.io.ASMemoryInStream;
import org.verapdf.cos.filters.COSFilterASCII85Decode;
import org.verapdf.cos.filters.COSFilterASCIIHexDecode;

import java.io.Closeable;
import java.io.IOException;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.verapdf.as.CharTable.*;

/**
 * Base PDF parser that operates with a buffered stream. The seek() operation
 * of stream is not required.
 *
 * @author Sergey Shemyakov
 */
public class NotSeekableBaseParser implements Closeable {

    private static final Logger LOGGER = Logger.getLogger(
            NotSeekableBaseParser.class.getCanonicalName());

    private static final byte ASCII_ZERO = 48;
    private static final byte ASCII_NINE = 57;

    // max string length in bytes
    private static final int MAX_STRING_LENGTH = 65535;

    // indicates if this parser is a postscript parser
    protected boolean isPSParser = false;

    protected ASBufferedInFilter source;
    private Token token;

    /**
     * Constructor from stream. New buffered stream from given stream is created.
     * @param stream is source data stream.
     */
    public NotSeekableBaseParser(ASInputStream stream) throws IOException {
        if (stream == null) {
            throw new IOException("Stream in NotSeekableBaseParser can't be null.");
        }
        this.source = new ASBufferedInFilter(stream);
        try {
            source.initialize();
        } catch (IOException e) {   // Someone have to close source in case of
            // initialization exception
            source.close();
            throw e;
        }
    }


    public NotSeekableBaseParser(ASInputStream fileStream, boolean isPSParser) throws IOException {
        this(fileStream);
        this.isPSParser = isPSParser;
    }


    /**
     * Closes source stream.
     */
    public void close() throws IOException {
        this.source.close();
    }

    // PROTECTED METHODS

    protected void initializeToken() {
        if (this.token == null) {
            this.token = new Token();
        }
    }

    private void appendToToken(final int ch) {
        this.token.append(ch);
    }

    protected Token getToken() {
        return this.token;
    }

    protected void readLine() throws IOException {
        initializeToken();
        this.token.clearValue();
        byte ch = this.source.readByte();
        while (!this.source.isEOF()) {
            if (ch == ASCII_LF || ch == ASCII_CR) {
                break;
            }
            appendToToken(ch);
            ch = this.source.readByte();
        }
    }

    protected byte[] getLineBytes() throws IOException {
        readLine();
        return this.token.getByteValue();
    }

    protected String readUntilDelimiter() throws IOException {
        initializeToken();
        this.token.clearValue();
        byte ch = this.source.readByte();
        while (!isSpace(ch) && !isTokenDelimiter(ch)) {
            appendToToken(ch);
            if (!this.source.isEOF()) {
                ch = this.source.readByte();
            } else {
                break;
            }
        }
        if (isSpace(ch) || isTokenDelimiter(ch)) {
            this.source.unread();
        }
        return this.token.getValue();
    }

    protected boolean findKeyword(final Token.Keyword keyword) throws IOException {
        nextToken();
        while (this.token.type != Token.Type.TT_EOF && (
                this.token.type != Token.Type.TT_KEYWORD || this.token.keyword != keyword)) {
            nextToken();
        }
        return this.token.type == Token.Type.TT_KEYWORD && this.token.keyword == keyword;
    }

    protected boolean findKeyword(final Token.Keyword keyword, final int lookUpSize) throws IOException {
        source.resetReadCounter();
        nextToken();
        while (this.token.type != Token.Type.TT_EOF && (this.token.type != Token.Type.TT_KEYWORD || this.token.keyword != keyword)) {
            if (this.source.getReadCounter() >= lookUpSize) {
                break;
            }
            nextToken();
        }
        return this.token.type == Token.Type.TT_KEYWORD && this.token.keyword == keyword;
    }

    protected void nextToken() throws IOException {
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
                    throw new IOException("Unknown symbol " + ch + " after \'>\'");
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

    protected void skipSpaces() throws IOException {
        this.skipSpaces(false);
    }

    protected void skipSpaces(boolean skipComment) throws IOException {
        byte ch;
        while (!this.source.isEOF()) {
            ch = this.source.readByte();
            if (CharTable.isSpace(ch)) {
                continue;
            }
            if (ch == '%' && skipComment) {
                skipComment();
                continue;
            }

            this.source.unread();
            break;
        }
    }

    protected void skipStreamSpaces() throws IOException {
        byte space = this.source.readByte();

        //check for whitespace
        while (space == ASCII_SPACE) {
            space = this.source.readByte();
        }

        if (space == ASCII_CR) {
            space = this.source.readByte();
            if (space != ASCII_LF) {
                this.source.unread();
            }
        } else if (space != ASCII_LF) {
            this.source.unread();
        }
    }

    protected boolean isDigit() throws IOException {
        return isDigit(this.source.peek());
    }

    protected static boolean isDigit(byte c) {
        return c >= ASCII_ZERO && c <= ASCII_NINE;
    }

    private void skipComment() throws IOException {
        // skips all characters till EOL == { CR, LF, CRLF }
        byte ch;
        while (!this.source.isEOF()) {
            ch = this.source.readByte();
            if (isLF(ch)) {
                return; // EOL == LF
            }

            if (isEndOfComment(ch)) {
                ch = this.source.readByte();
                if (!isLF(ch)) { // EOL == CR
                    this.source.unread();
                } // else EOL == CRLF
                return;
            }
            // else skip regular character
        }
    }

    protected boolean isEndOfComment(byte ch) {
        return isCR(ch);
    }

    protected static boolean isFF(int c) {
        return ASCII_FF == c;
    }

    protected static boolean isLF(int c) {
        return ASCII_LF == c;
    }

    protected static boolean isCR(int c) {
        return ASCII_CR == c;
    }

    private void readLitString() throws IOException {
        this.token.clearValue();

        int parenthesesDepth = 0;

        byte ch = this.source.readByte();
        while (!this.source.isEOF()) {
            switch (ch) {
                default:
                    appendToToken(ch);
                    break;
                case '(':
                    parenthesesDepth++;
                    appendToToken(ch);
                    break;
                case ')':
                    if (parenthesesDepth == 0) {
                        return;
                    }

                    parenthesesDepth--;
                    appendToToken(ch);
                    break;
                case '\\': {
                    ch = this.source.readByte();
                    switch (ch) {
                        case '(':
                            appendToToken(CharTable.ASCII_LEFT_PAR);
                            break;
                        case ')':
                            appendToToken(CharTable.ASCII_RIGHT_PAR);
                            break;
                        case 'n':
                            appendToToken(ASCII_LF);
                            break;
                        case 'r':
                            appendToToken(ASCII_CR);
                            break;
                        case 't':
                            appendToToken(CharTable.ASCII_HT);
                            break;
                        case 'b':
                            appendToToken(CharTable.ASCII_BS);
                            break;
                        case 'f':
                            appendToToken(CharTable.ASCII_FF);
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
                                appendToToken(ch1);
                            }
                            break;
                        }
                        case ASCII_LF:
                            break;
                        case ASCII_CR:
                            ch = this.source.readByte();
                            if (ch != ASCII_LF) {
                                this.source.unread();
                            }
                            break;
                        default:
                            appendToToken(ch);
                            break;
                    }
                    break;
                }
            }
            ch = source.readByte();
            if (token.getSize() > MAX_STRING_LENGTH) {
                LOGGER.log(Level.WARNING, "Content stream string token exceeds " + MAX_STRING_LENGTH + " bytes");
                break;
            }
        }
        while (!this.source.isEOF()) {
            switch (ch) {
                default:
                    break;
                case '(':
                    parenthesesDepth++;
                    break;
                case ')':
                    if (parenthesesDepth == 0) {
                        return;
                    }
                    parenthesesDepth--;
                    break;
                case '\\': {
                    ch = this.source.readByte();
                    switch (ch) {
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
                                for (int i = 1; i < 3; i++) {
                                    ch = this.source.readByte();
                                    if (ch < '0' || ch > '7') {
                                        this.source.unread();
                                        break;
                                    }
                                }
                            }
                            break;
                        }
                        case ASCII_CR:
                            ch = this.source.readByte();
                            if (ch != ASCII_LF) {
                                this.source.unread();
                            }
                            break;
                        default:
                            break;
                    }
                    break;
                }
            }

            ch = source.readByte();
        }
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
                this.token.setHexCount(Long.valueOf(hexCount));
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
        this.token.setHexCount(Long.valueOf(hexCount));
    }

    private void readASCII85() throws IOException {
        byte[] buf = new byte[ASBufferedInFilter.START_BUFFER_SIZE];
        int pointer = 0;
        byte readByte = this.source.readByte();
        while (!this.source.isEOF() && (readByte != '~' || this.source.peek() != '>')) {
            buf[pointer++] = readByte;
            if (pointer == buf.length) {
                buf = extendArray(buf);
            }
            readByte = this.source.readByte();
        }
        this.source.readByte();
        try (ASMemoryInStream ascii85Data = new ASMemoryInStream(Arrays.copyOf(buf, pointer));
             COSFilterASCII85Decode decoder = new COSFilterASCII85Decode(ascii85Data)) {
            byte[] res = new byte[buf.length];
            int read = decoder.read(res);

            this.token.setContainsOnlyHex(false);
            this.token.setHexCount(Long.valueOf(0));
            if (read == -1) {
                LOGGER.log(Level.WARNING, "Invalid ASCII85 string");
                this.token.clearValue();
            } else {
                this.token.setByteValue(Arrays.copyOf(res, read));
            }
        }
    }

    public static byte[] extendArray(byte[] array) {
        byte[] res = new byte[array.length * 2];
        System.arraycopy(array, 0, res, 0, array.length);
        return res;
    }

    private void readName() throws IOException {
        this.token.clearValue();
        byte ch;
        while (!this.source.isEOF()) {
            ch = this.source.readByte();
            if (CharTable.isTokenDelimiter(ch)) {
                this.source.unread();
                break;
            }

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
                        radix = Integer.valueOf(this.token.getValue());
                    }
                    token.clearValue();
                } else {
                    this.source.unread();
                    break;
                }
            }
            if (this.token.type == Token.Type.TT_INTEGER) {
                long value = Long.valueOf(this.token.getValue(), radix).longValue();
                this.token.integer = value;
                this.token.real = value;
            } else {
                double value = Double.valueOf(this.token.getValue()).doubleValue();
                this.token.integer = Math.round(value);
                this.token.real = value;
            }
        } catch (NumberFormatException e) {
            LOGGER.log(Level.FINE, "", e);
        }
    }
}
