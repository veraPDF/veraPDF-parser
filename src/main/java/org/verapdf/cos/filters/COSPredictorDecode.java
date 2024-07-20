/**
 * This file is part of veraPDF Parser, a module of the veraPDF project.
 * Copyright (c) 2015-2024, veraPDF Consortium <info@verapdf.org>
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

import org.verapdf.as.ASAtom;
import org.verapdf.as.filters.io.ASBufferedInFilter;
import org.verapdf.as.io.ASInputStream;
import org.verapdf.cos.COSDictionary;

import java.io.IOException;

/**
 * This filter represents predictor that is applied to Flate and LZW encodings.
 *
 * @author Sergey Shemyakov
 */
public class COSPredictorDecode extends ASBufferedInFilter {

    private static final byte PREDICTOR_DEFAULT = 1;
    private static final int COLORS_DEFAULT = 1;
    private static final int BITS_PER_COMPONENT_DEFAULT = 8;
    private static final int COLUMNS_DEFAULT = 1;

    private int bitsPerComponent;
    private int bytesPerChar;
    private int lineLength;
    private byte predictor;
    private byte[] previousLine = null;
    private boolean streamEnded = false;

    /**
     * Constructor from stream and decode parameters.
     *
     * @param stream       is unpredicted stream.
     * @param decodeParams is COSDictionary containing decode parameters.
     * @throws IOException
     */
    public COSPredictorDecode(ASInputStream stream,
                              COSDictionary decodeParams) throws IOException {
        super(stream);
        initializePredictorArguments(
                predictorFromParams(decodeParams),
                colorsFromParams(decodeParams),
                bitsFromParams(decodeParams),
                columnsFromParams(decodeParams));
    }

    private void initializePredictorArguments(byte predictor, int colors,
                                              int bitsPerComponent, int columns) {
        this.predictor = predictor;
        this.bitsPerComponent = bitsPerComponent;
        int bitsPerChar = colors * bitsPerComponent;
        this.bytesPerChar = (bitsPerChar + 7) / 8;
        this.lineLength = (columns * bitsPerChar + 7) / 8;
        if (previousLine == null) {
            previousLine = new byte[lineLength];
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int read(byte[] buffer, int size) throws IOException {
        if (streamEnded) {
            return -1;
        }
        if (this.bufferSize() == 0) {
            if (this.feedBuffer(getBufferCapacity()) == -1) {
                this.streamEnded = true;
            }
        }
        if (predictor == 1) {
            int popped = bufferPopArray(buffer, size);
            if (this.bufferSize() == 0) {
                if (this.feedBuffer(getBufferCapacity()) == -1) {
                    this.streamEnded = true;
                }
            }
            return popped;
        }
        int outputPointer = 0;
        byte linePredictor = predictor;
        byte[] currentLine = new byte[lineLength];

        while (!streamEnded) {

            // Determine if PNG predictor
            if (predictor >= 10) {
                if (bufferSize() == 0) {
                    if (this.feedBuffer(getBufferCapacity()) == -1) {
                        this.streamEnded = true;
                        break;
                    }
                }
                // each line starts with type 0 - 4
                linePredictor = bufferPop();
                linePredictor += 10;
            }

            int read;
            if ((read = bufferPopArray(currentLine, lineLength)) != lineLength) {
                if (this.feedBuffer(this.getBufferCapacity()) == -1) {
                    this.streamEnded = true;
                    break;
                }
                byte[] extraBytes = new byte[lineLength - read];
                int readAgain;
                if ((readAgain = bufferPopArray(extraBytes, extraBytes.length)) != extraBytes.length) {
                    this.streamEnded = true;
                    break;
                }
                System.arraycopy(extraBytes, 0, currentLine, read, readAgain);
            }

            switch (linePredictor) {
                case 2: // TIFF
                    if (bitsPerComponent == 16) {
                        for (int i = 0; i < lineLength; i += 2) {
                            int value = (currentLine[i] << 8) + currentLine[i + 1];
                            int left = i - bytesPerChar >= 0 ?
                                    ((currentLine[i - bytesPerChar] << 8) +
                                            currentLine[i - bytesPerChar + 1]) : 0;
                            currentLine[i] = (byte) ((value + left) >> 8);
                            currentLine[i + 1] = (byte) (value + left);
                        }
                        break;
                    } else if (bitsPerComponent == 8) {
                        for (int i = 0; i < lineLength; i++) {
                            byte value = currentLine[i];
                            byte left = i - bytesPerChar >= 0 ?
                                    currentLine[i - bytesPerChar] : 0;
                            currentLine[i] = (byte) (value + left);
                        }
                        break;
                    } else {
                        throw new IOException(bitsPerComponent + " bits per component can't be processed.");
                    }
                case 10: // None
                    break;
                case 11: // Sub
                    for (int i = 0; i < lineLength; i++) {
                        byte value = currentLine[i];
                        byte left = i - bytesPerChar >= 0 ?
                                currentLine[i - bytesPerChar] : 0;
                        currentLine[i] = (byte) (value + left);
                    }
                    break;
                case 12: // Up
                    for (int i = 0; i < lineLength; i++) {
                        byte value = currentLine[i];
                        byte up = previousLine[i];
                        currentLine[i] = (byte) (value + up);
                    }
                    break;
                case 13: // Avg
                    for (int i = 0; i < lineLength; i++) {
                        byte value = currentLine[i];
                        byte left = i - bytesPerChar >= 0 ?
                                currentLine[i - bytesPerChar] : 0;
                        byte up = previousLine[i];
                        currentLine[i] = (byte) (value + (left + up) / 2);
                    }
                    break;
                case 14: // Paeth
                    for (int i = 0; i < lineLength; i++) {
                        byte value = currentLine[i];
                        byte left = i - bytesPerChar >= 0 ?
                                currentLine[i - bytesPerChar] : 0;
                        byte up = previousLine[i];
                        byte upLeft = i - bytesPerChar >= 0 ?
                                previousLine[i - bytesPerChar] : 0;
                        int res = left + up - upLeft;
                        int leftDiff = Math.abs(res - left);
                        int upDiff = Math.abs(res - up);
                        int upLeftDiff = Math.abs(res - upLeft);

                        if (leftDiff <= upDiff && leftDiff <= upLeftDiff) {
                            currentLine[i] = (byte) (value + left);
                        } else if (upDiff <= upLeftDiff) {
                            currentLine[i] = (byte) (value + up);
                        } else {
                            currentLine[i] = (byte) (value + upLeft);
                        }
                    }
                    break;
                default:
                    break;
            }
            System.arraycopy(currentLine, 0, buffer, outputPointer, lineLength);
            System.arraycopy(currentLine, 0, previousLine, 0, lineLength);
            outputPointer += lineLength;
            if (outputPointer + lineLength > size) {
                return outputPointer;
            }
        }
        return outputPointer;
    }

    private static byte predictorFromParams(COSDictionary decodeParams) {
        if (decodeParams.knownKey(ASAtom.PREDICTOR)) {
            return (byte) decodeParams.getIntegerKey(ASAtom.PREDICTOR).intValue();
        } else {
            return PREDICTOR_DEFAULT;
        }
    }

    private static int colorsFromParams(COSDictionary decodeParams) {
        if (decodeParams.knownKey(ASAtom.COLORS)) {
            return decodeParams.getIntegerKey(ASAtom.COLORS).intValue();
        } else {
            return COLORS_DEFAULT;
        }
    }

    private static int bitsFromParams(COSDictionary decodeParams) {
        if (decodeParams.knownKey(ASAtom.BITS_PER_COMPONENT)) {
            return decodeParams.getIntegerKey(ASAtom.BITS_PER_COMPONENT).intValue();
        } else {
            return BITS_PER_COMPONENT_DEFAULT;
        }
    }

    private static int columnsFromParams(COSDictionary decodeParams) {
        if (decodeParams.knownKey(ASAtom.COLUMNS)) {
            return decodeParams.getIntegerKey(ASAtom.COLUMNS).intValue();
        } else {
            return COLUMNS_DEFAULT;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void reset() throws IOException {
        super.reset();
        this.streamEnded = false;
    }
}
