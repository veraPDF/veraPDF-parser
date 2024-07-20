/**
 * This file is part of veraPDF Parser, a module of the veraPDF project.
 * Copyright (c) 2015-2024, veraPDF Consortium <info@verapdf.org>
 * All rights reserved.
 *
 * veraPDF Parser is free software: you can redistribute it and/or modify
 * it under the terms of either:
 *
 * The GNU General public license GPLv3+.
 * You should have received a copy of the GNU General Public License
 * along with veraPDF Parser as the LICENSE.GPL file in the root of the source
 * tree.  If not, see http://www.gnu.org/licenses/ or
 * https://www.gnu.org/licenses/gpl-3.0.en.html.
 *
 * The Mozilla Public License MPLv2+.
 * You should have received a copy of the Mozilla Public License along with
 * veraPDF Parser as the LICENSE.MPL file in the root of the source tree.
 * If a copy of the MPL was not distributed with this file, you can obtain one at
 * http://mozilla.org/MPL/2.0/.
 */
package org.verapdf.parser;

import org.verapdf.as.filters.io.ASBufferedInFilter;
import org.verapdf.as.io.ASInputStream;
import org.verapdf.as.io.ASMemoryInStream;

import java.io.Closeable;
import java.io.IOException;
import java.util.Arrays;
import java.util.logging.Logger;

import static org.verapdf.as.CharTable.*;

/**
 * Base PDF parser that operates with a buffered stream. The seek() operation
 * of stream is not required.
 *
 * @author Sergey Shemyakov
 */
public class NotSeekableBaseParser extends BaseParser implements Closeable {

    private static final Logger LOGGER = Logger.getLogger(NotSeekableBaseParser.class.getCanonicalName());

    /**
     * Constructor from stream. New buffered stream from given stream is created.
     * @param stream is source data stream.
     */
    public NotSeekableBaseParser(ASInputStream stream) throws IOException {
        if (stream == null) {
            throw new IOException("Stream in NotSeekableBaseParser can't be null.");
        }
        this.source = new ASBufferedInFilter(stream);
        try {
            getSource().initialize();
        } catch (IOException e) {   // Someone have to close source in case of
            // initialization exception
            source.close();
            throw e;
        }
    }

    public NotSeekableBaseParser(ASInputStream fileStream, boolean isPSParser) throws IOException {
        this(fileStream);
        this.isPSParser = isPSParser;
    }

    /**
     * Closes source stream.
     */
    @Override
    public void close() throws IOException {
        this.source.close();
    }

    // PROTECTED METHODS

    @Override
    protected boolean findKeyword(final Token.Keyword keyword, final int lookUpSize) throws IOException {
        getSource().resetReadCounter();
        nextToken();
        while (this.token.type != Token.Type.TT_EOF && (this.token.type != Token.Type.TT_KEYWORD || this.token.keyword != keyword)) {
            if (this.getSource().getReadCounter() >= lookUpSize) {
                break;
            }
            nextToken();
        }
        return this.token.type == Token.Type.TT_KEYWORD && this.token.keyword == keyword;
    }

    protected void skipStreamSpaces() throws IOException {
        byte space = this.source.readByte();

        //check for whitespace
        while (space == ASCII_SPACE) {
            space = this.source.readByte();
        }

        if (space == ASCII_CR) {
            space = this.source.readByte();
        }
        if (space != ASCII_LF) {
            this.source.unread();
        }
    }

    @Override
    protected void skipComment() throws IOException {
        // skips all characters till EOL == { CR, LF, CRLF }
        while (!this.source.isEOF()) {
            byte ch = this.source.readByte();
            if (isLF(ch)) {
                return; // EOL == LF
            }

            if (isEndOfComment(ch)) {
                ch = this.source.readByte();
                if (!isLF(ch)) { // EOL == CR
                    this.source.unread();
                } // else EOL == CRLF
                return;
            }
            // else skip regular character
        }
    }

    protected boolean isEndOfComment(byte ch) {
        return isCR(ch);
    }

    @Override
    protected void readASCII85() throws IOException {
        byte[] buf = new byte[ASBufferedInFilter.START_BUFFER_SIZE];
        int pointer = 0;
        byte readByte = this.source.readByte();
        while (!this.source.isEOF() && (readByte != '~' || this.source.peek() != '>')) {
            buf[pointer++] = readByte;
            if (pointer == buf.length) {
                buf = extendArray(buf);
            }
            readByte = this.source.readByte();
        }
        this.source.readByte();
        try (ASMemoryInStream ascii85Data = new ASMemoryInStream(Arrays.copyOf(buf, pointer))) {
            decodeASCII85(ascii85Data, buf.length);
        }
    }

    public static byte[] extendArray(byte[] array) {
        byte[] res = new byte[array.length * 2];
        System.arraycopy(array, 0, res, 0, array.length);
        return res;
    }

    @Override
    protected ASBufferedInFilter getSource() {
        return (ASBufferedInFilter) source;
    }
}
