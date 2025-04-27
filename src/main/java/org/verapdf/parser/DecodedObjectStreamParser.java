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
public class DecodedObjectStreamParser extends SeekableCOSParser {

    private final COSStream objectStream;
    private final Map<Integer, Long> internalOffsets;

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
        keyOfCurrentObject = streamKey;
        try {
            calculateInternalOffsets();
        } catch (Exception e) {
            throw new IOException(getErrorMessage("Object stream has invalid N or First entry"), e);
        }
    }

    private void calculateInternalOffsets() throws IOException {
        int n = (int) ((COSInteger) this.objectStream.getKey(ASAtom.N).getDirectBase()).get();
        long first = ((COSInteger) this.objectStream.getKey(ASAtom.FIRST).getDirectBase()).get();
        for (int i = 0; i < n; ++i) {
            getBaseParser().skipSpaces(false);
            getBaseParser().readNumber();
            long objNum = getBaseParser().getToken().integer;
            getBaseParser().skipSpaces(false);
            getBaseParser().readNumber();
            long objOffset = getBaseParser().getToken().integer;
            internalOffsets.put((int) objNum, objOffset + first);
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

    /**
     * Parses object from object stream.
     *
     * @param key is key of object to parse. Object with this key should be
     *            present in this object stream.
     * @return object for given key or empty COSObject if key is not present.
     */
    public COSObject getObject(COSKey key) throws IOException {
        int objNum = key.getNumber();
        if (!this.internalOffsets.containsKey(objNum)) {
            return new COSObject();
        }
        this.getSource().seek(internalOffsets.get(objNum));
        this.flag = true;
        this.objects.clear();   // In case if some COSInteger was read before.
        this.integers.clear();
        this.keyOfCurrentObject = key;
        COSObject res = nextObject();
        res.setObjectKey(key);
        return res;
    }
}
