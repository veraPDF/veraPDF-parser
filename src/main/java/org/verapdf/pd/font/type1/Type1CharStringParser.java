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

import org.verapdf.as.io.ASInputStream;
import org.verapdf.pd.font.CFFNumber;

import java.io.IOException;

/**
 * This class parses charstring data in font Type 1 files after it was
 * eexec-decoded. In particular, it extracts glyph width info.
 *
 * @author Sergey Shemyakov
 */
public class Type1CharStringParser extends BaseCharStringParser {

    /**
     * {@inheritDoc}
     */
    public Type1CharStringParser(ASInputStream decodedCharString) throws IOException {
        super(decodedCharString);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean processNextOperator(int firstByte) throws IOException {
        if (firstByte != 12) {
            switch (firstByte) {
                case 14:    // endchar
                case 9:     // closepath
                case 11:    // return
                    break;
                case 6:     // hlineto
                case 22:    // hmoveto
                case 7:     // vlineto
                case 4:     // vmoveto
                    popStack(1);
                    break;
                case 5:     // rlineto
                case 21:    // rmoveto
                case 1:     // hstem
                case 3:     // vstem
                    popStack(2);
                    break;
                case 31:    // hvcurveto
                case 30:    // vhcurveto
                    popStack(4);
                    break;
                case 8:     // rrcurveto
                    popStack(6);
                    break;
                case 13:    //hsbw
                    if(!this.stack.empty()) {
                        this.setWidth(this.stack.pop());
                        popStack(1);
                    }
                    return true;
                case 10:    // callsubr
                    //TODO: should we parse this?
                    break;
                default:
                    break;
            }
        } else {
            byte[] buf = new byte[1];
            readStreams(buf, 1);
            switch (buf[0] & 0xFF) {
                case 0:     // dotsection
                    break;
                case 33:    // setcurrentpoint
                    popStack(2);
                    break;
                case 6:     // seac
                    popStack(5);
                    break;
                case 2:     // hstem3
                case 1:     // vstem3
                    popStack(6);
                    break;
                case 16:    // callothersubr
                    // TODO: implement call other subr
                    break;
                case 17:    // pop
                    popStack(1);
                    break;
                case 7:     // sbw
                    popStack(1);
                    this.setWidth(this.stack.pop());
                    popStack(2);
                    return true;
                case 12:    // div
                    int num2 = (int) this.stack.pop().getInteger();
                    int num1 = (int) this.stack.pop().getInteger();
                    this.stack.push(new CFFNumber(num1 / num2));  // That is not exactly what we should do, pushed number should be real. But we know that
                    break;  // width is integer, so the result of division is not needed.
                default:
                    break;
            }
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected CFFNumber readNextNumber(int firstByte) throws IOException {
        byte[] buf = new byte[4];
        readStreams(buf, 4);
        int res = 0;
        for (int i = 0; i < 3; ++i) {
            res |= (buf[i] & 0xFF);
            res <<= 8;
        }
        res |= buf[3] & 0xFF;
        return new CFFNumber(res);
    }
}
