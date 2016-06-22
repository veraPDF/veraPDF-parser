package org.verapdf.as.filters;

import org.verapdf.as.filters.io.ASBufferingInFilter;
import org.verapdf.as.io.ASInputStream;

import java.io.IOException;
import java.util.Arrays;

/**
 * Helper class containing methods for working with predictor in Flate and LZW
 * encodings.
 *
 * @author Sergey Shemyakov
 */
public class EncodingPredictorDecode extends ASBufferingInFilter {

    public EncodingPredictorDecode(ASInputStream stream) throws IOException {
        super(stream);
    }

    private static byte[] previousLine = null;

    public static EncodingPredictorResult decodePredictor(int predictor, int colors, int bitsPerComponent,
                                         int columns, byte[] input) throws IOException {

        int inputPointer = 0, outputPointer = 0, linePredictor = predictor;
        byte[] outputBuffer = new byte[ASBufferingInFilter.BF_BUFFER_SIZE];
        byte[] output = new byte[0];
        int bitsPerChar = colors * bitsPerComponent;
        int bytesPerChar = (bitsPerChar + 7) / 8;
        int lineLength = (columns * bitsPerChar + 7) / 8;
        byte[] currentLine;
        if(previousLine == null) {
            previousLine = new byte[lineLength];
        }
        while (inputPointer < input.length) {

            // Determine if PNG predictor
            if (predictor >= 10) {
                // each line starts with type 0 - 4
                linePredictor = input[inputPointer++];
                linePredictor += 10;
            }

            int currentLineEnd = Math.min(inputPointer + lineLength, input.length);
            if(currentLineEnd == inputPointer + lineLength) {
                currentLine = Arrays.copyOfRange(input, inputPointer, currentLineEnd);
                inputPointer += lineLength;
            } else {    // We have some final piece of bytes that does not form a full line. Do not process them.
                if(linePredictor > 10) {
                    inputPointer--;
                }
                byte[] remainingBytes;
                remainingBytes = Arrays.copyOfRange(input, inputPointer,
                        input.length);
                output = ASBufferingInFilter.concatenate(output, output.length,
                        outputBuffer, outputPointer);
                return new EncodingPredictorResult(Arrays.copyOf(output, output.length),
                        remainingBytes);
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
            System.arraycopy(currentLine, 0, outputBuffer, outputPointer, lineLength);
            System.arraycopy(currentLine, 0, previousLine, 0, lineLength);
            outputPointer += lineLength;
            if(outputPointer + lineLength >= outputBuffer.length) {
                output = ASBufferingInFilter.concatenate(output, output.length,
                        outputBuffer, outputPointer);   // If we are here, then data after prediction is less compact than before prediction, which is not common.
                outputPointer = 0;
            }
        }
        output = ASBufferingInFilter.concatenate(output, output.length,
                outputBuffer, outputPointer);
        return new EncodingPredictorResult(Arrays.copyOf(output, output.length),
                new byte[0]);
    }

    public static void resetPreviousLine() {
        previousLine = null;
    }
}
