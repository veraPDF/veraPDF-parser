package org.verapdf.pd.font.cff;

import org.verapdf.io.SeekableInputStream;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class handles cff charstrings.
 *
 * @author Sergey Shemyakov
 */
class CFFCharStringsHandler {

    private static final Logger LOGGER = Logger.getLogger(
            CFFCharStringsHandler.class.getCanonicalName());

    private static final int MAX_BUFFER_SIZE = 10240;

    private int amount;
    private CFFIndex memoryInCharStirngs;
    private SeekableInputStream fontStream;
    private long[] charStringsOffsets;

    CFFCharStringsHandler(CFFIndex charStrings, long charStringsOffset,
                          SeekableInputStream fontStream) {
        this.amount = charStrings.size();
        if (charStrings.getDataLength() < MAX_BUFFER_SIZE) {
            this.memoryInCharStirngs = charStrings;
        } else {
            this.fontStream = fontStream;
            this.charStringsOffsets = new long[charStrings.size() + 1];
            for(int i = 0; i < charStrings.size() + 1; ++i) {
                this.charStringsOffsets[i] = charStringsOffset +
                        charStrings.getOffsetShift() + charStrings.getOffset(i) - 1;
            }
        }
    }

    byte[] getCharString(int num) throws IOException {
        if(num >= 0 && num < this.amount) {
            if (memoryInCharStirngs != null) {
                return memoryInCharStirngs.get(num);
            } else {
                long offset = this.fontStream.getOffset();
                this.fontStream.seek(charStringsOffsets[num]);
                byte[] res = new byte[(int) (charStringsOffsets[num + 1] -
                        charStringsOffsets[num])];
                fontStream.read(res, res.length);
                fontStream.seek(offset);
                return res;
            }
        } else {
            LOGGER.log(Level.FINE, "Cannot obtain charstring " + num + ", " +
                    "total " + amount + "charstrings ");
            return new byte[]{};
        }
    }

    int getCharStringAmount() {
        return this.amount;
    }
}
