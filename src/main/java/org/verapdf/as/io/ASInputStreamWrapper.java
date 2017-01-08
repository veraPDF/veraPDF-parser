package org.verapdf.as.io;

import java.io.IOException;

/**
 * Class represents ASInputStream that can be constructed from another
 * ASInputStream.
 *
 * @author Sergey Shemyakov
 */
public class ASInputStreamWrapper extends ASInputStream {

    private ASInputStream stream;

    public ASInputStreamWrapper(ASInputStream stream) {
        this.stream = stream;
    }

    @Override
    public int read() throws IOException {
        return this.stream.read();
    }

    @Override
    public int read(byte[] buffer, int size) throws IOException {
        return this.stream.read(buffer, size);
    }

    @Override
    public int skip(int size) throws IOException {
        return this.stream.skip(size);
    }

    @Override
    public void reset() throws IOException {
        this.stream.reset();
    }

    @Override
    public void closeResource() throws IOException {
        this.stream.closeResource();
    }

    @Override
    public void close() throws IOException {
        this.stream.close();
    }
}
