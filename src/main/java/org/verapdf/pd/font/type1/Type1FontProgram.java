/**
 * This file is part of veraPDF Parser, a module of the veraPDF project.
 * Copyright (c) 2015, veraPDF Consortium <info@verapdf.org>
 * All rights reserved.
 * <p>
 * veraPDF Parser is free software: you can redistribute it and/or modify
 * it under the terms of either:
 * <p>
 * The GNU General public license GPLv3+.
 * You should have received a copy of the GNU General Public License
 * along with veraPDF Parser as the LICENSE.GPL file in the root of the source
 * tree.  If not, see http://www.gnu.org/licenses/ or
 * https://www.gnu.org/licenses/gpl-3.0.en.html.
 * <p>
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
import org.verapdf.cos.COSObject;
import org.verapdf.parser.COSParser;
import org.verapdf.parser.Token;
import org.verapdf.pd.font.FontProgram;
import org.verapdf.pd.font.truetype.TrueTypePredefined;
import org.verapdf.tools.resource.ASFileStreamCloser;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class does parsing of Type 1 font files.
 *
 * @author Sergey Shemyakov
 */
public class Type1FontProgram extends COSParser implements FontProgram {

    public static final Logger LOGGER =
            Logger.getLogger(Type1FontProgram.class.getCanonicalName());
    static final double[] DEFAULT_FONT_MATRIX = {0.001, 0, 0, 0.001, 0, 0};

    private double[] fontMatrix = Arrays.copyOf(DEFAULT_FONT_MATRIX, DEFAULT_FONT_MATRIX.length);
    private String[] encoding;
    private Map<String, Integer> glyphWidths;
    private static final byte[] CLEAR_TO_MARK_BYTES =
            Type1StringConstants.CLEARTOMARK_STRING.getBytes();
    private boolean attemptedParsing = false;
    private boolean successfullyParsed = false;

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
    public Type1FontProgram(InputStream fileStream)
            throws IOException {
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
        if (!attemptedParsing) {
            try {
                attemptedParsing = true;
                initializeToken();

                skipSpaces(true);

                while (getToken().type != Token.Type.TT_EOF) {
                    nextToken();
                    processToken();
                }
                if (glyphWidths == null) {
                    throw new IOException("Type 1 font doesn't contain charstrings.");
                }
                this.successfullyParsed = true;
            } finally {
                this.source.close();    // We close stream after first reading attempt
            }
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
                            String token = this.getToken().getValue();
                            if (token.equals(Type1StringConstants.DEF_STRING) ||
                                    token.equals(Type1StringConstants.READONLY_STRING)) {
                                break;
                            }
                            if (this.getToken().type == Token.Type.TT_EOF) {
                                throw new IOException("Can't parse Type 1 font program");
                            }
                            this.skipSpaces();
                            this.readNumber();
                            long key = this.getToken().integer;
                            this.nextToken();
                            if (key < 256) {
                                encoding[(int) key] = this.getToken().getValue();
                            } else {
                                LOGGER.log(Level.FINE, "Found glyph with encoding "
                                        + key + " in Type 1 font, value less than 256 expected.");
                            }
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
                        try (ASInputStream eexecEncoded = this.source.getStream(
                                this.source.getOffset(),
                                clearToMarkOffset - this.source.getOffset())) {
                            ASInputStream eexecDecoded = new EexecFilterDecode(
                                    eexecEncoded, false);
                            Type1PrivateParser parser = new Type1PrivateParser(
                                    eexecDecoded, fontMatrix);
                            try {
                                parser.parse();
                            } finally {
                                parser.closeInputStream();
                            }
                            this.glyphWidths = parser.getGlyphWidths();
                            this.source.seek(clearToMarkOffset);
                            break;
                        }
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
        while (!Arrays.equals(buf, CLEAR_TO_MARK_BYTES) && this.source.getOffset() > length) {
            this.source.unread(length + 1);
            this.source.read(buf, length);
        }
        if (this.source.getOffset() == length) {
            LOGGER.log(Level.FINE, "cleartomark keyword can't be found while parsing Type1 font.");
            this.source.seek(startingOffset);
            return source.getStreamLength();
        }
        long res = this.source.getOffset() - length;
        this.source.seek(startingOffset);
        return res - 512;
    }

    @Override
    public float getWidth(int charCode) {
        try {
            if (this.glyphWidths != null) {
                Integer res = this.glyphWidths.get(getGlyph(charCode));
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
        String glyphName = getGlyph(code);
        return this.glyphWidths != null &&
                this.glyphWidths.keySet().contains(glyphName);
    }

    @Override
    public boolean containsGlyph(String glyphName) {
        return this.glyphWidths != null &&
                this.glyphWidths.keySet().contains(glyphName);
    }

    @Override
    public boolean containsCID(int cid) {
        return false;
    }

    @Override
    public boolean isAttemptedParsing() {
        return this.attemptedParsing;
    }

    @Override
    public boolean isSuccessfulParsing() {
        return this.successfullyParsed;
    }

    public String[] getEncoding() {
        return encoding;
    }

    public Set<String> getCharSet() {
        return this.glyphWidths.keySet();
    }

    @Override
    public String getGlyphName(int code) {
        if (code > 0 && code < encoding.length) {
            return encoding[code];
        } else {
            return null;
        }
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

    private String getGlyph(int code) {
        if (code < encoding.length) {
            return encoding[code];
        } else {
            return TrueTypePredefined.NOTDEF_STRING;
        }
    }

    public ASFileStreamCloser getFontProgramResource() {
        if (this.source instanceof ASMemoryInStream) {
            return null;
        } else {
            return new ASFileStreamCloser(this.source);
        }
    }
}
