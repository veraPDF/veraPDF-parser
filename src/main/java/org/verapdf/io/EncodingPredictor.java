package org.verapdf.io;

import java.util.Arrays;

/**
 * Helper class containing methods for working with predictor in Flate and LZW
 * encodings.
 * @author Sergey Shemyakov
 */
public class EncodingPredictor {

    private EncodingPredictor() {
    }

    static byte [] decodePredictor(int predictor, int colors, int bitsPerComponent,
                                   int columns, byte [] input) {
        if(predictor == 1) {
            return Arrays.copyOf(input, input.length);
        } else {
            int bitsPerPixel = colors * bitsPerComponent;
            int bytesPerPixel = (bitsPerPixel + 7) / 8;
            int rowlength = (columns * bitsPerPixel + 7) / 8;
            int pointer = 0;
        }
    }
}
