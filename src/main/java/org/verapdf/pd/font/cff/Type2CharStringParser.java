/**
 * This file is part of veraPDF Parser, a module of the veraPDF project.
 * Copyright (c) 2015-2025, veraPDF Consortium <info@verapdf.org>
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
                }
                break;
            case 14:    // endchar
                if (!this.stack.empty() && this.stack.size() != 4) {
                    // If endchar is proceeded with 4 numbers, they are arguments
                    // for "seac" operator from charsting type 1.
                    this.setWidth(this.stack.get(0));
                }
                break;
            case 4:     // vmoveto
            case 22:    // hmoveto
                if (this.stack.size() > 1) {
                    this.setWidth(this.stack.get(0));
                }
                break;
            case 21:    // rmoveto
                if (this.stack.size() > 2) {
                    this.setWidth(this.stack.get(0));
                }
                break;
            case 1:     // hstem
            case 3:     // vstem
            case 18:    // hstemhm
            case 23:    // vstemhm
                if (this.stack.size() % 2 == 1) {
                    this.setWidth(this.stack.get(0));
                }
                break;
            case 28:    // actually not an operator but 2-byte number
                this.stack.push(readNextNumber(nextByte));
                return false;
            case 10:    // callsubr
                return execSubr(localSubrs, bias);
            case 29:    // callgsubr
                return execSubr(globalSubrs, gBias);
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
                break;
            case 11:    // return operator (may be founded in subroutines)
                return false;
            default:
                break;
        }
        return true;    // We can set width only by first operator.
    }

    private boolean execSubr(CFFIndex subrs, int bias) throws IOException {
        int subrNum = (int) this.stack.pop().getInteger();
        if (subrs.size() > Math.max(subrNum + bias, 0)) {
            byte[] subr = subrs.get(subrNum + bias);
            ASMemoryInStream subrStream = new ASMemoryInStream(subr, subr.length, false);
            addStream(subrStream);
        }
        return false;
    }

    @Override
    protected CFFNumber readNextNumber(int firstByte) throws IOException {
        byte[] buf = new byte[4];
        if (firstByte == 28) {
            readStreams(buf, 2);
            return new CFFNumber((short) (((buf[0] & 0xFF) << 8) | (buf[1] & 0xFF)));
        } else {
            readStreams(buf, 4);
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
