package org.verapdf.cos.filters;

import org.verapdf.as.filters.io.ASBufferingInFilter;
import org.verapdf.as.io.ASInputStream;

import java.io.IOException;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

/**
 * This class implements Flate decoding.
 *
 * @author Sergey Shemyakov
 */
public class COSFilterFlateDecode extends ASBufferingInFilter {

    private Inflater inflater;

    /**
     * Constructor from Flate encoded stream.
     *
     * @param stream is Flate encoded stream.
     * @throws IOException
     */
    public COSFilterFlateDecode(ASInputStream stream) throws IOException {
        super(stream);
        int bytesFed = (int) this.feedBuffer(getBufferCapacity());
        inflater = new Inflater();
        inflater.setInput(this.internalBuffer, 0, bytesFed);
    }

    /**
     * Decodes given flate compressed buffer and reads up to size bytes of
     * decompressed data.
     *
     * @param buffer is flate encoded data.
     * @param size   is maximal amount of decompressed bytes.
     * @return amount of actually read bytes.
     * @throws IOException
     */
    @Override
    public int read(byte[] buffer, int size) throws IOException {
        if (inflater.getRemaining() == 0) {
            int bytesFed = (int) this.feedBuffer(getBufferCapacity());
            if (bytesFed == -1) {
                return -1;
            }
            inflater.setInput(this.internalBuffer, 0, bytesFed);
        }
        try {
            return inflater.inflate(buffer, 0, size);
        } catch (DataFormatException e) {
            throw new IOException("Can't decode Flate encoded data", e);
        }
    }
}
