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
package org.verapdf.pd.font.type1;

import org.verapdf.as.CharTable;
import org.verapdf.as.io.ASInputStream;
import org.verapdf.as.io.ASMemoryInStream;
import org.verapdf.cos.COSKey;
import org.verapdf.parser.SeekableBaseParser;
import org.verapdf.parser.Token;
import org.verapdf.pd.font.CFFNumber;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class parses private data in font Type 1 files after it was
 * eexec-decoded. In particular, it extracts glyph info.
 *
 * @author Sergey Shemyakov
 */
class Type1PrivateParser extends SeekableBaseParser {

    private static final Logger LOGGER = Logger.getLogger(Type1PrivateParser.class.getCanonicalName());

    /**
     * An integer specifying the number of random bytes at the beginning
     * of charstrings for charstring encryption
     */
    private int lenIV;
    private Map<String, Integer> glyphWidths;
    private double[] fontMatrix;
    private boolean isDefaultFontMatrix;
    private boolean charStringsFound;
    private Map<Integer, CFFNumber> subrWidths;

    private COSKey key;

    /**
     * {@inheritDoc}
     */
    Type1PrivateParser(InputStream stream, double[] fontMatrix, COSKey key) throws IOException {
        super(stream);
        this.fontMatrix = fontMatrix;
        isDefaultFontMatrix = Arrays.equals(this.fontMatrix,
                Type1FontProgram.DEFAULT_FONT_MATRIX);
        this.lenIV = 4;
        this.charStringsFound = false;
        this.key = key;
    }

    /**
     * Parses private part of Type 1 font program.
     */
    public void parse() throws IOException {
        initializeToken();

        skipSpaces(true);

        while (getToken().type != Token.Type.TT_EOF &&
                (getToken().getValue() == null || !charStringsFound || !getToken().getValue().startsWith(Type1StringConstants.CLOSEFILE))) {
            nextToken();
            processToken();
        }
    }

    @Override
    protected void readName() throws IOException {
        this.clearToken();
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

    private void processToken() throws IOException {
        switch (this.getToken().type) {
            case TT_NAME:
                switch (this.getToken().getValue()) {
                    case Type1StringConstants.CHAR_STRINGS_STRING:
                        charStringsFound = true;
                        nextToken();
                        if (this.getToken().type != Token.Type.TT_INTEGER) {
                            break;
                        }
                        int amountOfGlyphs = (int) this.getToken().integer;
                        nextToken();    // reading "dict"
                        nextToken();    // reading "dup"
                        nextToken();    // reading "begin"
                        for (int i = 0; i < amountOfGlyphs; ++i) {
                            if (!decodeCharString()) {
                                break;
                            }
                        }
                        break;
                    case Type1StringConstants.LEN_IV_STRING:
                        this.nextToken();
                        if (this.getToken().type == Token.Type.TT_INTEGER) {
                            this.lenIV = (int) this.getToken().integer;
                        }
                        break;
                    case Type1StringConstants.SUBRS:
                        nextToken();
                        if (subrWidths == null) {
                            subrWidths = new HashMap<>();
                        }
                        int amountOfSubrs = (int) this.getToken().integer;
                        nextToken();    // reading "array"
                        for (int i = 0; i < amountOfSubrs; ++i) {
                            nextToken();    // reading "dup"
                            if (!this.getToken().getValue().equals(Type1StringConstants.DUP_STRING)) {
                                break;
                            }
                            nextToken();    // reading number
                            long number = this.getToken().integer;
                            nextToken();
                            long toSkip = this.getToken().integer;
                            skipRD();
                            this.skipSpaces();
                            long beginOffset = this.getSource().getOffset();
                            this.getSource().skip(toSkip);
                            try (ASInputStream chunk = this.getSource().getStream(beginOffset, toSkip);
                                 ASInputStream eexecDecode = new EexecFilterDecode(
                                         chunk, true, this.lenIV); ASInputStream decodedCharString = new ASMemoryInStream(eexecDecode)) {
                                Type1CharStringParser parser = new Type1CharStringParser(decodedCharString, subrWidths);
                                if (parser.getWidth() != null) {
                                    subrWidths.put((int) number, parser.getWidth());
                                }
                            }
                            this.nextToken();   // reading "NP"
                            // some fonts have 'noaccess put' instead of 'NP'. Supporting this case as well
                            if (this.getToken().getValue().equals(Type1StringConstants.NOACCESS)) {
                                this.nextToken(); // try to read in line put
                            }
                        }
                        break;
                    default:
                        break;
                }
        }
    }

    private boolean decodeCharString() throws IOException {
        if (glyphWidths == null) {
            this.glyphWidths = new HashMap<>();
        }
        this.nextToken();
        try {
            checkTokenType(Token.Type.TT_NAME);
        } catch (IOException e) {
            // There are files with wrong charstring amount specified. Actual
            // amount can be determined from "end" keyword.
            if (getToken().type == Token.Type.TT_KEYWORD && getToken().getValue().equals("end")) {
                LOGGER.log(Level.WARNING, getErrorMessage("Error in parsing private data in Type 1 font: incorrect amount of charstings specified"));
                return false;
            } else {
                throw e;
            }
        }
        String glyphName = this.getToken().getValue();
        this.nextToken();
        checkTokenType(Token.Type.TT_INTEGER);
        long charstringLength = this.getToken().integer;
        this.skipRD();
        this.skipSingleSpace();
        long beginOffset = this.getSource().getOffset();
        this.source.skip((int) charstringLength);
        try (ASInputStream chunk = this.getSource().getStream(beginOffset, charstringLength);
             ASInputStream eexecDecode = new EexecFilterDecode(
                     chunk, true, this.lenIV); ASInputStream decodedCharString = new ASMemoryInStream(eexecDecode)) {
            Type1CharStringParser parser = new Type1CharStringParser(decodedCharString, subrWidths);
            if (parser.getWidth() != null) {
                if (!isDefaultFontMatrix) {
                    glyphWidths.put(glyphName, applyFontMatrix(parser.getWidth().getReal()));
                } else {
                    glyphWidths.put(glyphName, (int) parser.getWidth().getInteger());
                }
            }
            this.nextToken();
        }
        return true;
    }

    private void checkTokenType(Token.Type expectedType) throws IOException {
        if (this.getToken().type != expectedType) {
            throw new IOException(getErrorMessage("Error in parsing Private dictionary of font 1" +
                    " file, expected type " + expectedType + ", but got " + this.getToken().type));
        }
    }

    private int applyFontMatrix(float width) {
        return (int) (width * (fontMatrix[0] * 1000));
    }

    Map<String, Integer> getGlyphWidths() {
        return glyphWidths;
    }

    private void skipRD() throws IOException {
        this.skipSpaces();
        nextToken();    // reading "RD"
        if (getToken().type == Token.Type.TT_INTEGER) { // we read "-" of "-|"
            int next = this.source.read();
            if (next != 124) {
                LOGGER.log(Level.FINE, getErrorMessage("Error in Type1 private parser in parsing RD in Subrs"));
            }
        }
    }

    @Override
    protected String getErrorMessage(String message, long offset) {
        if (key != null) {
            return message + "(offset = " + offset + " in stream " + key + ")";
        }
        return super.getErrorMessage(message, offset);
    }
}
