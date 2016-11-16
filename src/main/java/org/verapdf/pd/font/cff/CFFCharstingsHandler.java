package org.verapdf.pd.font.cff;

import org.verapdf.io.SeekableStream;

/**
 * This class handles cff charstrings.
 *
 * @author Sergey Shemyakov
 */
class CFFCharstingsHandler {

    private static final int MAX_BUFFER_SIZE = 10240;

    private CFFIndex memoryInCharstirngs;
    private SeekableStream fontStream;
    private long[] charstringsOffsets;

    CFFCharstingsHandler(CFFIndex charstrings, long charstringsOffset,
                         SeekableStream fontStream) {
        if (charstrings.getDataLength() < MAX_BUFFER_SIZE) {
            this.memoryInCharstirngs = charstrings;
        } else {
            this.fontStream = fontStream;
            this.charstringsOffsets = new long[charstrings.size() + 1];
            for(int i = 0; i < charstrings.size() + 1; ++i) {
                this.charstringsOffsets[i] = charstringsOffset +
                        charstrings.getOffsetShift() + charstrings.getOffset(i) - 1;
            }
        }
    }

    byte[] getCharstring(int num) {
        if (memoryInCharstirngs != null) {
            return memoryInCharstirngs.get(num);
        } else {

        }
    }

}
