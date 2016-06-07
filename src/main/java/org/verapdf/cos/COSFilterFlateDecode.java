package org.verapdf.cos;

import org.verapdf.as.filters.ASBufferingInFilter;
import org.verapdf.as.io.ASInputStream;

import java.io.IOException;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

/**
 * @author Sergey Shemyakov
 */
public class COSFilterFlateDecode extends ASBufferingInFilter{

    public COSFilterFlateDecode(ASInputStream stream) {
        super(stream);
    }

    /**
     * Decodes given flate compressed buffer and reads up to size bytes of
     * decompressed data.
     * @param buffer is flate encoded data.
     * @param size is maximal amount of decompressed bytes.
     * @return amount of actually read bytes.
     * @throws IOException
     */
    @Override
    public long read(byte[] buffer, long size) throws IOException {
        try {
            if (size == 0) {
                return 0;
            }
            Inflater inflater = new Inflater();
            inflater.setInput(this.internalBuffer);
            return inflater.inflate(buffer, 0, (int) size);
        } catch (DataFormatException ex) {
            throw new IOException("Can't inflate data", ex);
        }
    }
}
