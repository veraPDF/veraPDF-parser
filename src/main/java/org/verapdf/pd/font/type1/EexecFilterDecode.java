package org.verapdf.pd.font.type1;

import org.verapdf.as.filters.io.ASBufferingInFilter;
import org.verapdf.as.io.ASInputStream;

import java.io.IOException;

/**
 * This is filter that decodes eexec coding in type 1 font files.
 *
 * @author Sergey Shemyakov
 */
public class EexecFilterDecode extends ASBufferingInFilter {

    /*All these constants are defined in Adobe Type 1 Font Format
    Specification. See chapter 7 "Encryption".*/
    private static final int EEXEC_C1 = 52845;
    private static final int EEXEC_C2 = 22719;
    private static final int EEXEC_ENCRYPTION_KEY = 55665;
    private static final int EEXEC_NUMBER_OF_RANDOM_BYTES = 4;
    private static final int EEXEC_CHARSTRING_KEY = 4330;

    private int bytesToDiscard;
    private int r;

    /**
     * Constructor from stream.
     *
     * @param stream       is eexec-encoded stream.
     * @param isCharstring is true if passed stream is encoded charstring.
     * @param lenIV        is number of random bytes added to encoded data, value of
     *                     LenIV in Private dictionary.
     */
    public EexecFilterDecode(ASInputStream stream, boolean isCharstring,
                             int lenIV) throws IOException {
        super(stream);
        if (!isCharstring) {
            bytesToDiscard = EEXEC_NUMBER_OF_RANDOM_BYTES;
            r = EEXEC_ENCRYPTION_KEY;
        } else {
            bytesToDiscard = lenIV;
            r = EEXEC_CHARSTRING_KEY;
        }
    }

    /**
     * Constructor from stream.
     *
     * @param stream       is eexec-encoded stream.
     * @param isCharstring is true if passed stream is encoded charstring.
     */
    public EexecFilterDecode(ASInputStream stream, boolean isCharstring) throws IOException {
        this(stream, isCharstring, EEXEC_NUMBER_OF_RANDOM_BYTES);
    }

    /**
     * Decodes eexec encoded data and reads up to <code>size</code> encoded
     * bytes into buffer.
     *
     * @param buffer is array into which data will be decoded.
     * @param size   is maximal length of decoded data.
     * @return amount of actually read bytes.
     * @throws IOException
     */
    @Override
    public int read(byte[] buffer, int size) throws IOException {
        int bytesRead = (int) this.feedBuffer(bytesToDiscard + size);
        int res = bytesRead - bytesToDiscard;
        if (bytesRead == 0) {
            return -1;
        }
        for (int i = 0; i < bytesRead; ++i) {
            int encoded = this.internalBuffer[i] & 0xFF;
            int decoded = encoded ^ r >> 8;
            if (i >= bytesToDiscard) {
                buffer[i - bytesToDiscard] = (byte) decoded;
            }
            r = (encoded + r) * EEXEC_C1 + EEXEC_C2 & 0xffff;
        }
        bytesToDiscard = (bytesToDiscard - bytesRead) < 0 ? 0 :
                (bytesToDiscard - bytesRead);
        return res;
    }
}
