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

import org.verapdf.as.io.ASInputStream;
import org.verapdf.as.io.ASMemoryInStream;
import org.verapdf.pd.font.CFFNumber;
import org.verapdf.pd.font.type1.BaseCharStringParser;

import java.io.IOException;

/**
 * This class does basic parsing of Type 2 CharString to extract width value
 * from it.
 *
 * @author Sergey Shemyakov
 */
class Type2CharStringParser extends BaseCharStringParser {

    private static final int TWO_POWER_16 = 65536;

    /**
     * {@inheritDoc}
     */
    Type2CharStringParser(ASInputStream stream) throws IOException {
        super(stream);
    }

    /**
     * {@inheritDoc}
     */
    Type2CharStringParser(ASInputStream stream, CFFIndex localSubrs, int bias,
                          CFFIndex globalSubrs, int gBias) throws IOException {
        super(stream, localSubrs, bias, globalSubrs, gBias);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean processNextOperator(int nextByte) throws IOException {
        switch (nextByte) {
            case 19:    // cntrmask
            case 20:    // hintmask
                if (!this.stack.empty()) {
                    this.setWidth(this.stack.get(0));
                    return true;
                }
                break;
            case 14:    // endchar
                if (!this.stack.empty() && this.stack.size() != 4) {
                    // If endchar is proceeded with 4 numbers, they are arguments
                    // for "seac" operator from charsting type 1.
                    this.setWidth(this.stack.get(0));
                    return true;
                }
                break;
            case 4:     // vmoveto
            case 22:    // hmoveto
                if (this.stack.size() > 1) {
                    this.setWidth(this.stack.get(0));
                    return true;
                }
                this.stack.pop();
                break;
            case 21:    // rmoveto
                if (this.stack.size() > 2) {
                    this.setWidth(this.stack.get(0));
                    return true;
                }
                this.popStack(2);
                break;
            case 1:     // hstem
            case 3:     // vstem
            case 18:    // hstemhm
            case 23:    // vstemhm
                if (this.stack.size() % 2 == 1) {
                    this.setWidth(this.stack.get(0));
                    return true;
                }
                this.stack.clear();
                break;
            case 28:    // actually not an operator but 2-byte number
                this.stack.push(readNextNumber(nextByte));
                return false;
            case 10:    // subrcall
                int subrNum = (int) this.stack.pop().getInteger();
                if(this.stack.empty() && localSubrs.size() > subrNum + bias) {
                    CFFNumber subrWidth = getWidthFromSubroutine(localSubrs.get(subrNum + bias));
                    if (subrWidth != null) {
                        this.setWidth(subrWidth);
                    } else {
                        break;
                    }
                } else if (!this.stack.empty()) {
                    this.setWidth(this.stack.get(0));
                }
                return true;
            case 29:    // callgsubr
                subrNum = (int) this.stack.pop().getInteger();
                if(this.stack.empty() && globalSubrs.size() > subrNum + gBias) {
                    CFFNumber subrWidth = getWidthFromSubroutine(globalSubrs.get(subrNum + gBias));
                    if (subrWidth != null) {
                        this.setWidth(subrWidth);
                    } else {
                        break;
                    }
                } else if (!this.stack.empty()) {
                    this.setWidth(this.stack.get(0));
                }
                return true;
            case 5:     // rlineto
            case 6:     // hlineto
            case 7:     // vlineto
            case 8:     // rrcurveto
            case 27:    // hhcurveto
            case 24:    // rcurveline
            case 25:    // rlinecurve
            case 26:    // vvcurveto
            case 30:    // vhcurveto
            case 31:    // hvcurveto
                this.stack.clear();     // this is perfectly correct handling of stack in case of these ops
                break;
            case 11:    // return, not clear stack
                break;
            default:
                this.stack.clear();     // this is more of a hack. May not be fully correct, but correct enough
                break;
        }
        return true;    // We can set width only by first operator.
    }

    private CFFNumber getWidthFromSubroutine(byte[] subr) throws IOException {
        ASMemoryInStream subrStream = new ASMemoryInStream(subr, subr.length, false);
        Type2CharStringParser parser = new Type2CharStringParser(subrStream,
                this.localSubrs, this.bias, this.globalSubrs, this.gBias);
        if (parser.getWidth() != null) {
            return parser.getWidth();
        } else {
            for (int i = 0; i < parser.stack.size(); ++i) {
                this.stack.push(parser.stack.get(i));
            }
            return null;
        }
    }

    @Override
    protected CFFNumber readNextNumber(int firstByte) throws IOException {
        byte[] buf = new byte[4];
        if (firstByte == 28) {
            this.stream.read(buf, 2);
            return new CFFNumber((char) (((buf[0] & 0xFF) << 8) | (buf[1] & 0xFF)));
        } else {
            this.stream.read(buf, 4);
            int integer = 0;
            for (int i = 0; i < 3; ++i) {
                integer |= (buf[i] & 0xFF);
                integer <<= 8;
            }
            integer |= buf[3] & 0xFF;
            float res = integer;
            return new CFFNumber(res / TWO_POWER_16);
        }
    }
}
