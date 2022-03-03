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

import org.verapdf.as.ASAtom;
import org.verapdf.as.CharTable;
import org.verapdf.as.io.ASInputStream;
import org.verapdf.cos.COSArray;
import org.verapdf.cos.COSObjType;
import org.verapdf.cos.COSObject;
import org.verapdf.parser.Token;
import org.verapdf.parser.postscript.PSObject;
import org.verapdf.parser.postscript.PSOperator;
import org.verapdf.parser.postscript.PSParser;
import org.verapdf.parser.postscript.PostScriptException;
import org.verapdf.pd.font.FontProgram;
import org.verapdf.pd.font.truetype.TrueTypePredefined;
import org.verapdf.pd.function.PSOperatorsConstants;
import org.verapdf.tools.resource.ASFileStreamCloser;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class does parsing of Type 1 font files.
 *
 * @author Sergey Shemyakov
 */
public class Type1FontProgram extends PSParser implements FontProgram {

    public static final Logger LOGGER =
            Logger.getLogger(Type1FontProgram.class.getCanonicalName());
    static final double[] DEFAULT_FONT_MATRIX = {0.001, 0, 0, 0.001, 0, 0};

    private String[] encoding;
    private Map<String, Integer> glyphWidths;
    private static final byte[] CLEAR_TO_MARK_BYTES =
            Type1StringConstants.CLEARTOMARK_STRING.getBytes(StandardCharsets.ISO_8859_1);
    private boolean attemptedParsing = false;
    private boolean successfullyParsed = false;

    public static final Set<String> OPERATORS_KEYWORDS;

    static {
        Set<String> tempSet = new HashSet<>();
        tempSet.add(PSOperatorsConstants.ABS);
        tempSet.add(PSOperatorsConstants.FLOOR);
        tempSet.add(PSOperatorsConstants.MOD);
        tempSet.add(PSOperatorsConstants.ADD);
        tempSet.add(PSOperatorsConstants.IDIV);
        tempSet.add(PSOperatorsConstants.MUL);
        tempSet.add(PSOperatorsConstants.DIV);
        tempSet.add(PSOperatorsConstants.NEG);
        tempSet.add(PSOperatorsConstants.SUB);
        tempSet.add(PSOperatorsConstants.CEILING);
        tempSet.add(PSOperatorsConstants.ROUND);
        tempSet.add(PSOperatorsConstants.COPY);
        tempSet.add(PSOperatorsConstants.EXCH);
        tempSet.add(PSOperatorsConstants.POP);
        tempSet.add(PSOperatorsConstants.DUP);
        tempSet.add(PSOperatorsConstants.INDEX);
        tempSet.add(PSOperatorsConstants.ROLL);
        tempSet.add(PSOperatorsConstants.CLEAR);
        tempSet.add(PSOperatorsConstants.COUNT);
        tempSet.add(PSOperatorsConstants.MARK);
        tempSet.add(PSOperatorsConstants.CLEARTOMARK);
        tempSet.add(PSOperatorsConstants.COUNTTOMARK);
        tempSet.add(PSOperatorsConstants.DICT);
        tempSet.add(PSOperatorsConstants.BEGIN);
        tempSet.add(PSOperatorsConstants.LENGTH);
        tempSet.add(PSOperatorsConstants.DEF);
        tempSet.add(PSOperatorsConstants.LOAD);
        tempSet.add(PSOperatorsConstants.ARRAY);
        tempSet.add(PSOperatorsConstants.PUT);
        tempSet.add(PSOperatorsConstants.FOR);
        tempSet.add(PSOperatorsConstants.STANDARD_ENCODING);

        OPERATORS_KEYWORDS = Collections.unmodifiableSet(tempSet);
    }

    /**
     * {@inheritDoc}
     */
    public Type1FontProgram(ASInputStream fileStream)
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

                // PFB format check
                byte first = this.source.readByte();
                if (first == -128) {
                    byte second = this.source.readByte();
                    if (second == 1) {
                        LOGGER.log(Level.WARNING, "Type 1 fonts in PFB format are not permitted");
                    }
                    this.source.unread();
                }
                this.source.unread();

                initializeToken();

                skipSpaces(true);

                while (getToken().type != Token.Type.TT_EOF) {
                    processObject(nextObject());
                }
                initializeEncoding();
                if (glyphWidths == null) {
                    throw new IOException("Type 1 font doesn't contain charstrings.");
                }
                this.successfullyParsed = true;
            } catch (PostScriptException e) {
                throw new IOException("Error in PostScript parsing", e);
            } finally {
                this.source.close();    // We close stream after first reading attempt
            }
        }
    }

    private void processObject(COSObject nextObject) throws IOException, PostScriptException {
        if (nextObject.getType() == COSObjType.COS_NAME &&
                nextObject.getString().equals(Type1StringConstants.EEXEC_STRING)) {
            this.skipSpacesExceptNullByte();
            Type1PrivateParser parser = null;
            try (ASInputStream eexecEncoded = this.source.getStreamUntilToken(
                    CLEAR_TO_MARK_BYTES)) {
                try (ASInputStream eexecDecoded = new EexecFilterDecode(
                        eexecEncoded, false)) {
                    parser = new Type1PrivateParser(
                            eexecDecoded, getFontMatrix());
                    parser.parse();
                    this.glyphWidths = parser.getGlyphWidths();
                } finally {
                    if (parser != null) {
                        parser.closeInputStream();
                    }
                }
            }
        } else {
            toExecute(nextObject);
        }
    }

    private void toExecute(COSObject next) throws PostScriptException {
        PSObject operator = PSObject.getPSObject(next);
        if (operator instanceof PSOperator) {
            if (!OPERATORS_KEYWORDS.contains(((PSOperator) operator).getOperator())) {
                COSObject dictEntry = userDict.get(ASAtom.getASAtom(((PSOperator) operator).getOperator()));
                if (dictEntry != null) {
                    toExecute(dictEntry);
                }
            } else {
                operator.execute(operandStack, userDict);
            }
        } else {
            operator.execute(operandStack, userDict);
        }
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

    protected void skipSpacesExceptNullByte() throws IOException {
        byte ch;
        while (!this.source.isEOF()) {
            ch = this.source.readByte();
            if (CharTable.isSpace(ch) && ch != 0) {
                continue;
            }

            this.source.unread();
            break;
        }
    }

    @Override
    public boolean containsCode(int code) {
        String glyphName = getGlyph(code);
        return containsGlyph(glyphName);
    }

    @Override
    public boolean containsGlyph(String glyphName) {
        return this.glyphWidths != null &&
                this.glyphWidths.keySet().contains(glyphName) &&
                !glyphName.equals(TrueTypePredefined.NOTDEF_STRING);
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

    /**
     * @return encoding from embedded font program as array of strings.
     * encoding[i] = glyphName <-> i has name glyphName.
     */
    public String[] getEncoding() {
        return encoding;
    }

    /**
     * @return charset from embedded program, i. e. set of all glyph names
     * defined in the embedded font program.
     */
    public Set<String> getCharSet() {
        return this.glyphWidths.keySet();
    }

    private double[] getFontMatrix() {
        COSObject fontMatrixObject = this.getObjectFromUserDict(ASAtom.getASAtom(
                Type1StringConstants.FONT_MATRIX_STRING));
        if (fontMatrixObject != null && fontMatrixObject.getType() == COSObjType.COS_ARRAY) {
            double[] res = new double[6];
            int pointer = 0;
            for (COSObject obj : ((COSArray) fontMatrixObject.get())) {
                if (obj.getType().isNumber()) {
                    res[pointer++] = obj.getReal();
                }
            }
            return res;
        }
        return DEFAULT_FONT_MATRIX;
    }

    private void initializeEncoding() {
        COSObject encoding = this.getObjectFromUserDict(ASAtom.getASAtom(
                Type1StringConstants.ENCODING_STRING));
        if (encoding != null) {
            if (encoding.getType() == COSObjType.COS_ARRAY) {
                int pointer = 0;
                for (COSObject obj : ((COSArray) encoding.get())) {
                    if (pointer < 256) {
                        String glyphName = obj.getString();
                        this.encoding[pointer++] = glyphName == null ? "" : glyphName;
                    }
                }
            } else if (encoding.getType() == COSObjType.COS_NAME) {
                if (Type1StringConstants.STANDARD_ENCODING_STRING.equals(encoding.getString())) {
                    this.encoding = TrueTypePredefined.STANDARD_ENCODING;
                }
            }
        }
    }

    @Override
    public String getGlyphName(int code) {
        if (code > 0 && code < encoding.length) {
            return encoding[code];
        } else {
            return null;
        }
    }

    private String getGlyph(int code) {
        if (code < encoding.length) {
            return encoding[code];
        } else {
            return TrueTypePredefined.NOTDEF_STRING;
        }
    }

    /**
     * @return the closeable object that closes source stream of this font
     * program.
     */
    public ASFileStreamCloser getFontProgramResource() {
        return new ASFileStreamCloser(this.source);
    }

    @Override
    public String getWeight() {
        return null;
    }
}
