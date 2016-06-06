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

    @Override
    public int read(byte[] buffer, int size) throws IOException {
        try {
            if (size == 0) {
                return 0;
            }
            Inflater inflater = new Inflater();
            inflater.setInput(this.internalBuffer);
            return inflater.inflate(buffer);
        } catch (DataFormatException ex) {
            throw new IOException("Can't inflate data", ex);
        }
    }
}
