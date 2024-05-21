package org.verapdf.parser;

import java.io.IOException;

/**
 * @author Maxim Plushchov
 */
public interface BaseParserInputStream {

    int read() throws IOException;

    int read(byte[] buffer) throws IOException;

    byte readByte() throws IOException;

    void unread() throws IOException;

    void unread(int i) throws IOException;

    int peek() throws IOException;

    int skip(int size) throws IOException;

    void close() throws IOException;

    boolean isEOF() throws IOException;
}
