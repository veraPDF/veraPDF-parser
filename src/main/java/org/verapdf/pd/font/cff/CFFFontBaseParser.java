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
package org.verapdf.pd.font.cff;

import org.verapdf.as.io.ASMemoryInStream;
import org.verapdf.io.SeekableInputStream;
import org.verapdf.pd.font.CFFNumber;
import org.verapdf.tools.resource.ASFileStreamCloser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This is base class for CFF CID font and CFF Type 1 font parsers.
 *
 * @author Sergey Shemyakov
 */
abstract class CFFFontBaseParser extends CFFFileBaseParser {

    private static final Logger LOGGER = Logger.getLogger(CFFFontBaseParser.class.getCanonicalName());

    protected boolean attemptedParsing = false;
    protected boolean successfullyParsed = false;

    protected static final float[] DEFAULT_FONT_MATRIX =
            {(float) 0.001, 0, 0, (float) 0.001, 0, 0};
    protected ArrayList<CFFNumber> stack;
    protected CFFIndex globalSubrs;
    protected boolean isSubset;

    //Top DICT
    protected long privateDictOffset;
    protected long privateDictSize;
    protected long topDictBeginOffset;
    protected long topDictEndOffset;
    protected float[] fontMatrix;
    protected long charStringsOffset;
    protected long charSetOffset;
    protected int charStringType;
    protected String weight;

    //CharStrings
    protected int nGlyphs;
    protected CFFIndex charStrings;
    protected CharStringsWidths widths;

    //Private DICT
    protected int defaultWidthX;
    protected int nominalWidthX;

    //Subrs
    protected long subrsOffset = -1;

    public CFFFontBaseParser(SeekableInputStream source) {
        super(source);
        stack = new ArrayList<>(48);
        this.charStringType = 2;
        this.charSetOffset = 0; // default
    }

    protected void readTopDictUnit() throws IOException {
        try {
            int next = this.source.peek();
            if ((next > 27 && next < 31) || (next > 31 && next < 255)) {
                this.stack.add(readNumber());
            } else {
                this.source.readByte();
                if (next < 22) {
                    switch (next) {
                        case 4:
                            this.weight =
                                    getStringBySID((int) this.stack.get(this.stack.size() - 1).getInteger());
                            this.stack.clear();
                            break;
                        case 15:    // charset
                            this.charSetOffset =
                                    this.stack.get(this.stack.size() - 1).getInteger();
                            this.stack.clear();
                            break;
                        case 17:    // CharStrings
                            this.charStringsOffset =
                                    this.stack.get(stack.size() - 1).getInteger();
                            this.stack.clear();
                            break;
                        case 18:    // Private
                            this.privateDictSize =
                                    this.stack.get(stack.size() - 2).getInteger();
                            this.privateDictOffset =
                                    this.stack.get(stack.size() - 1).getInteger();
                            this.stack.clear();
                            break;
                        case 12:
                            next = this.source.readByte() & 0xFF;
                            switch (next) {
                                case 7:     // FontMatrix
                                    fontMatrix = new float[6];
                                    for (int i = 0; i < 6; ++i) {
                                        fontMatrix[i] = this.stack.get(i).getReal();
                                    }
                                    this.stack.clear();
                                    break;
                                case 6:     // Charstring Type
                                    this.charStringType = (int)
                                            this.stack.get(this.stack.size() - 1).getInteger();
                                    this.stack.clear();
                                    break;
                                default:
                                    readTopDictTwoByteOps(next);
                            }
                            break;
                        default:
                            readTopDictOneByteOps(next);
                    }
                }
            }
        } catch (IndexOutOfBoundsException e) {
            throw new IOException("Error with stack in processing Top DICT in CFF file", e);
        }
    }

    protected void readTopDictTwoByteOps(int lastRead) throws IOException {
        this.stack.clear();
    }

    protected void readTopDictOneByteOps(int lastRead) {
        this.stack.clear();
    }

    protected void readCharStrings() throws IOException {
        this.charStrings = this.readIndex();
        this.nGlyphs = this.charStrings.size();
    }

    protected void readPrivateDictUnit() throws IOException {
        int next = this.source.peek();
        if ((next > 27 && next < 31) || (next > 31 && next < 255)) {
            this.stack.add(readNumber());
        } else {
            this.source.readByte();
            if (next < 22) {
                switch (next) {
                    case 20:    // defaultWidthX
                        if (!this.stack.isEmpty()) {
                            this.defaultWidthX = (int)
                                    this.stack.get(stack.size() - 1).getInteger();
                            this.stack.clear();
                        } else {
                            LOGGER.log(Level.FINE, "Empty arguments stack for defaultWidthX operator");
                        }
                        break;
                    case 21:    // nominalWidthX
                        if (!this.stack.isEmpty()) {
                            this.nominalWidthX = (int)
                                    this.stack.get(stack.size() - 1).getInteger();
                            this.stack.clear();
                        } else {
                            LOGGER.log(Level.FINE, "Empty arguments stack for nominalWidthX operator");
                        }
                        break;
                    case 19:    // Subrs
                        if (!this.stack.isEmpty()) {
                            this.subrsOffset = this.stack.get(stack.size() - 1).getInteger()
                                               + privateDictOffset;
                            this.stack.clear();
                        } else {
                            LOGGER.log(Level.FINE, "Empty arguments stack for Subrs operator");
                        }
                        break;
                    default:
                        this.stack.clear();
                }
            }
        }
    }

    /**
     * @return amount of glyphs in the font.
     */
    public int getNGlyphs() {
        return this.nGlyphs;
    }

    public ASFileStreamCloser getFontProgramResource() {
        if (this.source instanceof ASMemoryInStream) {
            return null;
        } else {
            return new ASFileStreamCloser(this.source);
        }
    }

    public String getWeight() {
        return weight;
    }
}
