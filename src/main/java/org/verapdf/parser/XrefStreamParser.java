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
import org.verapdf.as.filters.io.ASBufferedInFilter;
import org.verapdf.as.io.ASInputStream;
import org.verapdf.cos.*;
import org.verapdf.cos.xref.COSXRefEntry;
import org.verapdf.cos.xref.COSXRefInfo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * This class parses xref stream to obtain xref entries with object numbers,
 * generations and byte offsets.
 *
 * @author Sergey Shemyakov
 */
class XrefStreamParser {

    private COSXRefInfo section;
    private COSStream xrefCOSStream;

    /**
     * Constructor.
     *
     * @param section       is xref section, where xref entries and trailer
     *                      information will be written.
     * @param xrefCOSStream is xref COSStream.
     */
    XrefStreamParser(COSXRefInfo section, COSStream xrefCOSStream) {
        this.section = section;
        this.xrefCOSStream = xrefCOSStream;
    }

    /**
     * This is an entry point for parsing xref stream and trailer.
     *
     * @throws IOException
     */
    void parseStreamAndTrailer() throws IOException {
        try (ASInputStream xrefInputStream = xrefCOSStream.getData(COSStream.FilterFlags.DECODE)) {
            COSObject indexObject = initializeIndex();
            List<Long> objIDs = initializeObjIDs(indexObject);
            parseStream(xrefInputStream, objIDs);
            setTrailer();
        }
    }

    /**
     * This method makes sure that Index array is correctly initialized.
     *
     * @throws IOException
     */
    private COSObject initializeIndex() throws IOException {
        COSObject indexObject = xrefCOSStream.getKey(ASAtom.INDEX);

        if (indexObject.empty()) {
            COSObject[] defaultIndex = new COSObject[2];
            defaultIndex[0] = COSInteger.construct(0);
            defaultIndex[1] = xrefCOSStream.getKey(ASAtom.SIZE);
            indexObject = COSArray.construct(2, defaultIndex);
        } else if (indexObject.getType() != COSObjType.COS_ARRAY || indexObject.size() % 2 != 0) {
            throw new IOException("Index array in xref stream has odd amount of elements.");
        }
        return indexObject;
    }

    /**
     * This method calculates object ID for all objects, described in this xref
     * stream using Index array.
     */
    private List<Long> initializeObjIDs(COSObject indexObject) throws IOException {
        List<Long> objIDs = new ArrayList<>();
        for (int i = 0; i < indexObject.size(); i += 2) {
            Long firstID = indexObject.at(i).getInteger();
            Long lengthOfSubsection = indexObject.at(i + 1).getInteger();
            if (firstID == null || lengthOfSubsection == null) {
                throw new IOException("Failed to initialize objects ids");
            }
            for (int j = 0; j < lengthOfSubsection; ++j) {
                objIDs.add(firstID + j);
            }
        }
        return objIDs;
    }

    /**
     * This method does low-level parsing of xref stream.
     *
     * @throws IOException
     */
    private void parseStream(ASInputStream xrefInputStream, List<Long> objIDs) throws IOException {
        COSObject sizesObject = xrefCOSStream.getKey(ASAtom.W);
        if (sizesObject.getType() != COSObjType.COS_ARRAY || sizesObject.size() != 3) {
            throw new IOException("W array in xref shall have 3 elements.");
        }
        Long field0Size = sizesObject.at(0).getInteger();
        Long field1Size = sizesObject.at(1).getInteger();
        Long field2Size = sizesObject.at(2).getInteger();
        if (field0Size == null || field1Size == null || field2Size == null) {
            throw new IOException("Object of W array shall contain an Integer");
        }
        byte[] field0 = new byte[field0Size.intValue()];
        byte[] field1 = new byte[field1Size.intValue()];
        byte[] field2 = new byte[field2Size.intValue()];
        byte[] buffer;
        byte[] remainedBytes = new byte[0];
        int objIdIndex = 0;

        while (true) {
            buffer = new byte[ASBufferedInFilter.BF_BUFFER_SIZE];
            long read = xrefInputStream.read(buffer, ASBufferedInFilter.BF_BUFFER_SIZE);
            if (read == -1) {
                break;
            }
            buffer = ASBufferedInFilter.concatenate(remainedBytes, remainedBytes.length,
                    buffer, (int) read);

            int pointer = 0;
            COSXRefEntry xref;
            for (; objIdIndex < objIDs.size(); ++objIdIndex) {
                if(pointer + field0.length + field1.length + field2.length >
                        buffer.length) {
                    remainedBytes = Arrays.copyOfRange(buffer, pointer, buffer.length);
                    break;
                }
                Long id = objIDs.get(objIdIndex);
                System.arraycopy(buffer, pointer, field0, 0, field0.length);
                pointer += field0.length;
                System.arraycopy(buffer, pointer, field1, 0, field1.length);
                pointer += field1.length;
                System.arraycopy(buffer, pointer, field2, 0, field2.length);
                pointer += field2.length;
                int type = 1;   // Default value for type
                if (field0.length > 0) {
                    type = (int) numberFromBytes(field0);
                }
                switch (type) {
                    case 0:
                        break;
                    case 1:
                        xref = new COSXRefEntry();
                        xref.offset = numberFromBytes(field1);
                        if (field2.length > 0) {
                            xref.generation = (int) numberFromBytes(field2);
                        } else {
                            xref.generation = 0;
                        }
                        section.getXRefSection().add(new COSKey(id.intValue(),
                                xref.generation), xref.offset);
                        break;
                    case 2:
                        xref = new COSXRefEntry();
                        xref.offset = -numberFromBytes(field1);
                        if (field2.length > 0) {
                            xref.generation = 0;
                        }
                        section.getXRefSection().add(new COSKey(id.intValue(),
                                xref.generation), xref.offset);
                        break;
                    default:
                        throw new IOException("Error in parsing xref stream");
                }
            }
        }
    }

    /**
     * This method puts all necessary information into trailer of this xref
     * section.
     */
    private void setTrailer() {
        COSTrailer trailer = section.getTrailer();
        if (xrefCOSStream.getKey(ASAtom.SIZE).get() != null) {
            trailer.setSize(((COSInteger) xrefCOSStream.getKey(ASAtom.SIZE).get()).get());
        }
        if (xrefCOSStream.getKey(ASAtom.PREV).get() != null) {
            trailer.setPrev(((COSInteger) xrefCOSStream.getKey(ASAtom.PREV).get()).get());
        }
        if (xrefCOSStream.getKey(ASAtom.ROOT).get() != null) {
            trailer.setRoot(xrefCOSStream.getKey(ASAtom.ROOT));
        }
        if (xrefCOSStream.getKey(ASAtom.ENCRYPT).get() != null) {
            trailer.setEncrypt(xrefCOSStream.getKey(ASAtom.ENCRYPT));
        }
        if (xrefCOSStream.getKey(ASAtom.INFO).get() != null) {
            trailer.setInfo(xrefCOSStream.getKey(ASAtom.INFO));
        }
        if (xrefCOSStream.getKey(ASAtom.ID).get() != null) {
            trailer.setID(xrefCOSStream.getKey(ASAtom.ID));
        }
    }

    /**
     * This is a helper method for low-level parsing, it converts number
     * represented with array of bytes into long.
     *
     * @param num is byte array to be converted.
     * @return long obtained from given bytes.
     */
    private static long numberFromBytes(byte[] num) {
        long res = 0;
        for (int i = 0; i < num.length; ++i) {
            res += (num[i] & 0x00FF) << ((num.length - i - 1) * 8);
        }
        return res;
    }
}
