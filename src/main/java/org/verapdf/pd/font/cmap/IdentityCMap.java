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
package org.verapdf.pd.font.cmap;

import java.io.IOException;
import java.io.InputStream;

/**
 * Class represents identity CMap.
 *
 * @author Sergey Shemyakov
 */
public class IdentityCMap extends CMap {

    private static final String ADOBE = "Adobe";
    private static final String IDENTITY = "Identity";
    private static final int DEFAULT_SUPPLEMENT = 0;

    public IdentityCMap(String name) {
        this.setName(name);
    }

    @Override
    public int toCID(int character) {
        return character;
    }

    @Override
    public boolean containsCode(int character) {
        return character != 0;
    }

    @Override
    public int getCodeFromStream(InputStream stream) throws IOException {
        int firstByte = stream.read();
        int secondByte = stream.read();
        return (firstByte << 8) + secondByte;
    }

    @Override
    public String getRegistry() {
        return ADOBE;
    }

    @Override
    public String getOrdering() {
        return IDENTITY;
    }

    @Override
    public int getSupplement() {
        return DEFAULT_SUPPLEMENT;
    }

    @Override
    public String getUnicode(int code) {
        return null;
    }
}
