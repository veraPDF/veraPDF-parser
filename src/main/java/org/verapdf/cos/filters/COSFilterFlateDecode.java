package org.verapdf.cos.filters;

import org.verapdf.as.filters.io.ASBufferingInFilter;
import org.verapdf.as.io.ASInputStream;
import org.verapdf.io.ASMemoryInStream;

import java.io.IOException;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

/**
 * @author Sergey Shemyakov
 */
public class COSFilterFlateDecode extends ASBufferingInFilter {

    Inflater inflater;

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
        if(inflater.getRemaining() == 0) {
            int bytesFed = (int) this.feedBuffer(getBufferCapacity());
            if(bytesFed == -1) {
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

    protected void decode() throws IOException {    // TODO: add here checking of size of decoded data and, possibly, decoding into file.
        byte[] encodedData = new byte[0];
        byte[] buffer = new byte[BF_BUFFER_SIZE];
        int read = this.getInputStream().read(buffer, BF_BUFFER_SIZE);
        while (read != -1) {
            encodedData = concatenate(encodedData, encodedData.length,
                    buffer, read);
            read = this.getInputStream().read(buffer, BF_BUFFER_SIZE);
        }

        byte[] decodedData = new byte[0];
        Inflater inflater = new Inflater();
        inflater.setInput(encodedData);
        buffer = new byte[BF_BUFFER_SIZE];
        try {
            while (true) {
                read = inflater.inflate(buffer);
                if (read != 0) {
                    decodedData = concatenate(decodedData,
                            decodedData.length, buffer, read);
                    continue;
                }
                if (inflater.finished() || inflater.needsDictionary()) {
                    break;
                }
            }
        } catch (DataFormatException e) {
            throw new IOException("Can't decode Flate encoded data", e);
        }
        this.setInputStream(new ASMemoryInStream(decodedData, decodedData.length, false));
    }
}
