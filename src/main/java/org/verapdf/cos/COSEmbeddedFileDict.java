/**
 * This file is part of veraPDF Parser, a module of the veraPDF project.
 * Copyright (c) 2015-2025, veraPDF Consortium <info@verapdf.org>
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
package org.verapdf.cos;

import org.verapdf.as.ASAtom;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents embedded file dictionary accessible via EF key in a file
 * specification dictionary (see PDF 32000-2008, table 44).
 *
 * @author Sergey Shemyakov
 */
public class COSEmbeddedFileDict {

    private final COSDictionary dictionary;
    private static final List<ASAtom> DEFINED_FILE_KEYS;

    static {
        DEFINED_FILE_KEYS = new ArrayList<>();
        DEFINED_FILE_KEYS.add(ASAtom.F);
        DEFINED_FILE_KEYS.add(ASAtom.UF);
        DEFINED_FILE_KEYS.add(ASAtom.DOS);
        DEFINED_FILE_KEYS.add(ASAtom.MAC);
        DEFINED_FILE_KEYS.add(ASAtom.UNIX);
    }

    public COSEmbeddedFileDict(COSDictionary dictionary) {
        this.dictionary = dictionary;
    }

    /**
     * @return a list of streams for available embedded files.
     */
    public List<COSStream> getEmbeddedFileStreams() {
        List<COSStream> res = new ArrayList<>();
        for (ASAtom fileKey : DEFINED_FILE_KEYS) {
            COSObject fileStream = dictionary.getKey(fileKey);
            if (!fileStream.empty() && fileStream.getType() == COSObjType.COS_STREAM) {
                res.add((COSStream) fileStream.getDirectBase());
            }
        }
        return Collections.unmodifiableList(res);
    }

}
