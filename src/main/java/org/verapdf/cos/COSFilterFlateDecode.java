package org.verapdf.cos;

import org.verapdf.as.filters.io.ASBufferingInFilter;
import org.verapdf.as.io.ASInputStream;

import java.io.IOException;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

/**
 * @author Sergey Shemyakov
 */
public class COSFilterFlateDecode extends ASBufferingInFilter {

    private final Inflater inflater = new Inflater();

    public COSFilterFlateDecode(ASInputStream stream) throws IOException {
        super(stream);
        long dataLength = feedBuffer(getBufferCapacity());
        inflater.setInput(this.internalBuffer, 0, (int) dataLength);
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
        try {
            if (size == 0 || inflater.finished() || inflater.needsDictionary()) {
                return 0;
            }
            return inflater.inflate(buffer, 0, size);
        } catch (DataFormatException ex) {
            throw new IOException("Can't inflate data", ex);
        }
    }

    protected void decode() throws IOException {
        byte [] encodedData = new byte[0];
        long read = feedBuffer(getBufferCapacity());
        while(read != 0) {
            encodedData = concatenate(encodedData, encodedData.length,
                    internalBuffer, (int) read);
            read = feedBuffer(getBufferCapacity());
        }
        byte[] buffer = new byte[2048];
        byte[] decodedStream = new byte[0];
        while (true) {
            read = read(buffer, 2048);
            if (read == 0) {
                break;
            }
            decodedStream = concatenate(decodedStream, decodedStream.length, buffer, (int) read);
        }
        this.getInputStream() = new
    }

    private static byte[] concatenate(byte[] one, int lengthOne, byte[] two, int lengthTwo) {  // Can return passed array, use with care
        if (lengthOne == 0) {
            if(lengthTwo != two.length) {
                return Arrays.copyOfRange(two, 0, lengthTwo);
            } else {
                return two;
            }
        }
        if (lengthTwo == 0) {
            if(lengthOne != one.length) {
                return Arrays.copyOfRange(one, 0, lengthOne);
            } else {
                return one;
            }
        }
        byte[] res = new byte[lengthOne + lengthTwo];
        System.arraycopy(one, 0, res, 0, lengthOne);
        System.arraycopy(two, 0, res, lengthOne, lengthTwo);
        return res;
    }
}
