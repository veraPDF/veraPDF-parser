package org.verapdf.tools.resource;

import java.io.Closeable;
import java.io.IOException;

/**
 * Class-wrapper around closeable streams that allows only to close streams.
 *
 * @author Sergey Shemyakov
 */
public class ASFileStreamCloser implements Closeable {

    private Closeable stream;

    public ASFileStreamCloser(Closeable stream) {
        this.stream = stream;
    }

    @Override
    public void close() throws IOException {
        if (this.stream != null) {
            this.stream.close();
        }
    }

    Closeable getStream() {
        return this.stream;
    }
}