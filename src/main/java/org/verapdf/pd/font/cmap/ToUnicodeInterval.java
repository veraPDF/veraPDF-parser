package org.verapdf.pd.font.cmap;

import java.io.UnsupportedEncodingException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Represents interval of mappings to Unicode.
 *
 * @author Sergey Shemyakov
 */
public class ToUnicodeInterval {

    private static final Logger LOGGER = Logger.getLogger(ToUnicodeInterval.class.getCanonicalName());

    private long intervalBegin;
    private long intervalEnd;
    private long startingValue;

    public ToUnicodeInterval(long intervalBegin, long intervalEnd, long startingValue) {
        this.intervalBegin = intervalBegin;
        this.intervalEnd = intervalEnd;
        this.startingValue = startingValue;
    }

    public boolean containsCode(long code) {
        return code >= intervalBegin && code <= intervalEnd;
    }

    public String toUnicode(int code) {
        long unicode = code - intervalBegin + startingValue;
        return getUnicodeNameFromLong(unicode);
    }

    private static String getUnicodeNameFromLong(long unicode) {
        byte[] arr = new byte[2];
        arr[1] = (byte) (unicode & 0xFF);
        unicode >>= 8;
        arr[0] = (byte) (unicode & 0xFF);
        try {
            if (arr[0] == 0) {
                return String.valueOf(arr[1]);
            }
			return new String(arr, "UTF-16BE");
        } catch (UnsupportedEncodingException e) {
            LOGGER.log(Level.FINE, "Can't find String encoding UTF-16BE", e);
            return null;    // I'm sure this won't be reached
        }
    }
}
