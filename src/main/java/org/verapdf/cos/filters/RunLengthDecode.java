/*
 * This file is part of veraPDF Parser, a module of the veraPDF project.
 * Copyright (c) 2015-2026, veraPDF Consortium <info@verapdf.org>
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
package org.verapdf.cos.filters;

import org.verapdf.as.filters.io.ASBufferedInFilter;
import org.verapdf.as.io.ASInputStream;

import java.io.IOException;

/**
 * @author Maxim Plushchov
 */
public class RunLengthDecode extends ASBufferedInFilter {
    private boolean streamEnded = false;
    private byte[] leftoverBuffer = new byte[0];
    private int leftoverSize = 0;

    public RunLengthDecode(ASInputStream stream) throws IOException {
        super(stream);
    }

    @Override
    public int read(byte[] buffer, int size) throws IOException {
        if (streamEnded) {
            return -1;
        }
        int outputPointer = 0;
        if (leftoverSize > 0) {
            if (leftoverSize >= size) {
                System.arraycopy(leftoverBuffer, leftoverBuffer.length - leftoverSize, buffer, 0, size);
                leftoverSize -= size;
                return size;
            }
            System.arraycopy(leftoverBuffer, leftoverBuffer.length - leftoverSize, buffer, 0, leftoverSize);
            outputPointer = leftoverSize;
            leftoverSize = 0;
        }
        if (this.bufferSize() == 0 && this.feedBuffer(getBufferCapacity()) == -1) {
            this.streamEnded = true;
        }
        while (!streamEnded) {
            if (bufferSize() == 0 && this.feedBuffer(getBufferCapacity()) == -1) {
                this.streamEnded = true;
                break;
            }
            int b = bufferPop();
            if (b >= 0) {
                if (bufferSize() == 0 && this.feedBuffer(getBufferCapacity()) == -1) {
                    this.streamEnded = true;
                    break;
                }
                int count = b + 1;
                byte[] data = new byte[count];
                int read = bufferPopArray(data, count);
                while (read != count) {
                    if (this.feedBuffer(this.getBufferCapacity()) == -1) {
                        this.streamEnded = true;
                        break;
                    }
                    byte[] extraBytes = new byte[count - read];
                    int readAgain = bufferPopArray(extraBytes, extraBytes.length);
                    System.arraycopy(extraBytes, 0, data, read, readAgain);
                    read += readAgain;
                }
                if (streamEnded) {
                    break;
                }
                int leftBufferSize = size - outputPointer;
                if (count > leftBufferSize) {
                    System.arraycopy(data, 0, buffer, outputPointer, leftBufferSize);
                    leftoverSize = count - leftBufferSize;
                    leftoverBuffer = new byte[leftoverSize];
                    System.arraycopy(data, leftBufferSize, leftoverBuffer, 0, leftoverSize);
                    return size;
                }
                System.arraycopy(data, 0, buffer, outputPointer, count);
                outputPointer += count;
            } else {
                int runLength = -b + 1;
                if (bufferSize() == 0 && this.feedBuffer(getBufferCapacity()) == -1) {
                    this.streamEnded = true;
                    break;
                }
                byte value = bufferPop();
                int leftBufferSize = size - outputPointer;
                int count = Math.min(leftBufferSize, runLength);
                for (int i = 0; i < count; i++) {
                    buffer[outputPointer++] = value;
                }
                if (runLength > leftBufferSize) {
                    leftoverSize = runLength - leftBufferSize;
                    leftoverBuffer = new byte[leftoverSize];
                    for (int i = 0; i < leftoverSize; i++) {
                        leftoverBuffer[i] = value;
                    }
                    return size;
                }
            }
        }
        return outputPointer;
    }
}
