package org.verapdf.cos;

import org.verapdf.as.filters.ASBufferingOutFilter;
import org.verapdf.as.io.ASOutputStream;

import java.io.IOException;
import java.util.zip.Deflater;

/**
 * @author Sergey Shemyakov
 */
public class COSFilterFlateEncode extends ASBufferingOutFilter {

    public COSFilterFlateEncode(ASOutputStream stream) {
        super(stream);
    }

    /**
     * Flate encodes given data buffer.
     * @param buffer is buffer to be encoded.
     * @return length of encoded data buffer.
     * @throws IOException
     */
    @Override
    public long write(byte[] buffer) throws IOException {
        Deflater deflater = new Deflater();
        deflater.setInput(buffer);
        deflater.finish();
        return deflater.deflate(internalBuffer);
    }
}
