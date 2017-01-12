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
package org.verapdf.parser;

import org.verapdf.as.ASAtom;
import org.verapdf.as.io.ASInputStream;
import org.verapdf.cos.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class reads objects from decoded object stream.
 *
 * @author Sergey Shemyakov
 */
public class DecodedObjectStreamParser extends COSParser {

    private COSStream objectStream;
    private Map<Integer, Long> internalOffsets;

    /**
     * Constructor from decoded object stream data and COSStream.
     *
     * @param inputStream  contains decoded object stream.
     * @param objectStream is COSStream that is being parsed.
     * @param streamKey    is key of given COSStream.
     * @throws IOException
     */
    public DecodedObjectStreamParser(final ASInputStream inputStream,
                                     COSStream objectStream,
                                     COSKey streamKey, COSDocument doc) throws IOException {
        super(doc, inputStream);
        this.objectStream = objectStream;
        this.internalOffsets = new HashMap<>();
        try {
            calculateInternalOffsets();
        } catch (IOException e) {
            throw new IOException("Object stream " + streamKey.getNumber() + " "
                    + streamKey.getGeneration() + " has invalid N value", e);
        }
    }

    private void calculateInternalOffsets() throws IOException {
        int n = (int) ((COSInteger) this.objectStream.getKey(ASAtom.N).getDirectBase()).get();
        long first = ((COSInteger) this.objectStream.getKey(ASAtom.FIRST).getDirectBase()).get();
        for (int i = 0; i < n; ++i) {
            Long objNum, objOffset;
            skipSpaces(false);
            readNumber();
            objNum = getToken().integer;
            skipSpaces(false);
            readNumber();
            objOffset = getToken().integer;
            internalOffsets.put(objNum.intValue(), objOffset + first);
        }
    }

    /**
     * @return true if object stream contains object with number <code>objNum</code>.
     */
    public boolean containsObject(int objNum) {
        return this.internalOffsets.containsKey(objNum);
    }

    /**
     * @return list of keys of all objects, contained inside this object stream.
     */
    public List<COSKey> getInternalObjectsKeys() {
        List<COSKey> res = new ArrayList<>();
        for (Map.Entry<Integer, Long> entry : internalOffsets.entrySet()) {
            res.add(new COSKey(entry.getKey(), 0));  // Object inside streams shall have generation 0
        }
        return res;
    }

    public COSObject getObject(int objNum) throws IOException {
        if (!this.internalOffsets.containsKey(objNum)) {
            return new COSObject();
        }
        this.source.seek(internalOffsets.get(objNum));
        this.flag = true;
        return nextObject();
    }
}
