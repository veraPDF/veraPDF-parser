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
import org.verapdf.pd.font.cff.CFFIndex;

import java.io.IOException;
import java.util.Stack;

/**
 * This is base class for Type1CharStringParser and Type2CharStringParser.
 *
 * @author Sergey Shemyakov
 */
public abstract class BaseCharStringParser {

    private Stack<ASInputStream> streams;
    protected Stack<CFFNumber> stack;
    private CFFNumber width;

    protected CFFIndex globalSubrs;
    protected CFFIndex localSubrs;
    protected int bias;
    protected int gBias;

    /**
     * Constructor that calls method parse(), so width is extracted right after
     * object is created. Subroutines are ignored in this case.
     *
     * @param stream is stream with decoded CharString.
     * @throws IOException if parsing fails.
     */
    protected BaseCharStringParser(ASInputStream stream) throws IOException {
        this(stream, null, 0, null, 0);
    }

    /**
     * Constructor that calls method parse(), so width is extracted right after
     * object is created.
     *
     * @param stream     is stream with decoded CharString.
     * @param localSubrs is local subroutines for this CharString.
     * @param bias       is bias value for local subroutines as it is described
     *                   in The Compact Font Format specification.
     * @param gBias      is bias value for global subroutines as it is described
     *                   in The Compact Font Format specification.
     * @throws IOException if parsing fails.
     */
    protected BaseCharStringParser(ASInputStream stream, CFFIndex localSubrs,
                                   int bias, CFFIndex globalSubrs, int gBias) throws IOException {
        this.streams = new Stack<>();
        this.streams.push(stream);
        this.stack = new Stack<>();
        this.width = null;
        this.globalSubrs = globalSubrs == null ? CFFIndex.getEmptyIndex() : globalSubrs;
        this.localSubrs = localSubrs == null ? CFFIndex.getEmptyIndex() : localSubrs;
        this.bias = bias;
        this.gBias = gBias;
        parse();
    }

    /**
     * @return width of glyph or null if it can't be found in given CharString.
     */
    public CFFNumber getWidth() {
        return this.width;
    }

    protected void setWidth(CFFNumber width) {
        this.width = width;
    }

    /**
     * Methods reads next charstring-encoded number from stream.
     *
     * @param firstByte is first byte of encoded integer. Note that this byte is
     *                  already read.
     * @return number that was read.
     * @throws IOException if stream reading error occurs.
     */
    private CFFNumber getNextInteger(int firstByte) throws IOException {
        byte[] buf = new byte[1];
        if (firstByte > 31 && firstByte < 247) {
            return new CFFNumber(firstByte - 139);
        } else if (firstByte > 246 && firstByte < 251) {
            readStreams(buf, 1);
            return new CFFNumber(((firstByte - 247) << 8)
                    + (buf[0] & 0xFF) + 108);
        } else if (firstByte > 250 && firstByte < 255) {
            readStreams(buf, 1);
            return new CFFNumber(-((firstByte - 251) << 8) -
                    (buf[0] & 0xFF) - 108);
        } else {
            return readNextNumber(firstByte);
        }
    }

    /**
     * This method does all the parsing needed to extract width from CharString.
     *
     * @throws IOException if stream reading error occurs.
     */
    private void parse() throws IOException {
        byte[] buf = new byte[1];
        int cont = readStreams(buf, 1);
        while (cont != -1) {
            int nextByte = buf[0] & 0xFF;
            if (nextByte > 31) {
                this.stack.push(getNextInteger(nextByte));
            } else {
                if (processNextOperator(nextByte)) {
                    break;
                }
            }
            cont = readStreams(buf, 1);
        }
        clear();
    }

    protected int readStreams(byte[] buffer, int size) throws IOException {
        if (buffer.length < size) {
            throw new IOException("Can't write bytes into passed buffer: too small.");
        }
        if (this.streams.empty()) {
            return -1;
        }
        int bufferIndex = 0;
        int left = size;
        while(left != 0 && !this.streams.empty()) {
            byte[] temp = new byte[left];
            int read = this.streams.peek().read(temp, left);
            if (read > 0) {
                System.arraycopy(temp, 0, buffer, bufferIndex, read);
                bufferIndex += read;
                left -= read;
            } else if (read == -1) {
                this.streams.pop().close();
            }
        }

        int read = size - left;
        return read == 0 ? -1 : read;
    }

    protected void addStream(ASInputStream is) {
        if (is != null) {
            this.streams.push(is);
        }
    }

    private void clear() throws IOException {
        this.stack.clear();
        while(!this.streams.empty()) {
            this.streams.pop().close();
        }
    }

    /**
     * Pops several operands from Type 1 Build Char stack.
     *
     * @param num is amount of numbers to be popped.
     * @throws IOException if stream reading error occurs.
     */
    protected void popStack(int num) {
        for (int i = 0; i < num && !this.stack.empty(); ++i) {
            this.stack.pop();
        }
    }

    /**
     * This method processes charstring-encoded operators. It should set width
     * when it can be determined. Methods returns true if width is calculated.
     *
     * @param nextByte is first byte of operator. Note that this byte is already
     *                 read.
     * @return true if width was extracted from processed operator.
     * @throws IOException if stream reading error occurs.
     */
    protected abstract boolean processNextOperator(int nextByte) throws IOException;

    /**
     * This method reads next bytes from stream and interprets them as one
     * number. In Type 1 CharStrings and Type 2 CharStrings this is done a
     * little differently.
     *
     * @return number that was read.
     * @throws IOException if stream reading error occurs.
     */
    protected abstract CFFNumber readNextNumber(int firstByte) throws IOException;
}
