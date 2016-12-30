package org.verapdf.tools.resource;

import org.verapdf.as.io.ASInputStream;

import java.io.IOException;

/**
 * @author Sergey Shemyakov
 */
public class ClosableASInputStreamWrapper extends ASInputStream{

    private ASInputStream stream;
    private boolean isClosed = false;

    public ClosableASInputStreamWrapper(ASInputStream stream) {
        this.stream = stream;
    }

    @Override
    public int read() throws IOException {
        this.checkClosed("Reading");
        return this.stream.read();
    }

    @Override
    public int read(byte[] buffer, int size) throws IOException {
        this.checkClosed("Reading");
        return this.stream.read(buffer, size);
    }

    @Override
    public int skip(int size) throws IOException {
        this.checkClosed("Skipping");
        return this.stream.skip(size);
    }

    @Override
    public void reset() throws IOException {
        this.stream.reset();
    }

    @Override
    public void close() throws IOException {
        this.isClosed = true;
    }

    private void checkClosed(String streamUsage) throws IOException {
        if (isClosed) {
            throw new IOException(streamUsage + " can't be performed; stream is closed");
        }
    }
}
