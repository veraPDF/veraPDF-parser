/**
 * This file is part of veraPDF Parser, a module of the veraPDF project.
 * Copyright (c) 2015, veraPDF Consortium <info@verapdf.org>
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
        stream.incrementResourceUsers();
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
    public int read(byte[] b, int off, int len) throws IOException {
        return this.stream.read(b, off, len);
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
        if (!isClosed) {
            decrementResourceUsers();
            isClosed = true;
            if (this.stream.resourceUsers.equals(0)) {
                this.stream.close();
            }
        }
    }

    @Override
    public void incrementResourceUsers() {
        this.stream.incrementResourceUsers();
    }

    @Override
    public void decrementResourceUsers() {
        this.stream.decrementResourceUsers();
    }
}
