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
import org.verapdf.as.filters.io.ASBufferingInFilter;
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

    private COSArray index;
    private ASInputStream xrefInputStream;
    private COSArray fieldSizes;
    private List<Long> objIDs;
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

        try {
            xrefInputStream = xrefCOSStream.getData(COSStream.FilterFlags.DECODE);
            fieldSizes = (COSArray) xrefCOSStream.getKey(ASAtom.W).getDirectBase();
            if (fieldSizes.size() != 3) {
                throw new IOException("W array in xref should have 3 elements.");
            }
            initializeIndex();
            initializeObjIDs();
            parseStream();
            setTrailer();
        } finally {
            xrefInputStream.close();
        }
    }

    /**
     * This method makes sure that Index array is correctly initialized.
     *
     * @throws IOException
     */
    private void initializeIndex()
            throws IOException {
        index = (COSArray) xrefCOSStream.getKey(ASAtom.INDEX).getDirectBase();

        if (index == null) {
            COSObject[] defaultIndex = new COSObject[2];
            defaultIndex[0] = COSInteger.construct(0);
            defaultIndex[1] = xrefCOSStream.getKey(ASAtom.SIZE);
            index = (COSArray) COSArray.construct(2, defaultIndex).getDirectBase();
        } else if (index.size() % 2 != 0) {
            throw new IOException("Index array in xref stream has odd amount of elements.");
        }
    }

    /**
     * This method calculates object ID for all objects, described in this xref
     * stream using Index array.
     */
    private void initializeObjIDs() {
        objIDs = new ArrayList<>();
        for (int i = 0; i < index.size() / 2; ++i) {
            COSInteger firstID = (COSInteger) index.at(2 * i).getDirectBase();
            COSInteger lengthOfSubsection = (COSInteger) index.at(2 * i + 1).getDirectBase();
            for (int j = 0; j < lengthOfSubsection.get(); ++j) {
                objIDs.add(firstID.get() + j);
            }
        }
    }

    /**
     * This method does low-level parsing of xref stream.
     *
     * @throws IOException
     */
    private void parseStream() throws IOException {
        byte[] field0 = new byte[fieldSizes.at(0).getInteger().intValue()];
        byte[] field1 = new byte[fieldSizes.at(1).getInteger().intValue()];
        byte[] field2 = new byte[fieldSizes.at(2).getInteger().intValue()];
        byte[] buffer;
        byte[] remainedBytes = new byte[0];
        int objIdIndex = 0;

        while (true) {
            buffer = new byte[ASBufferingInFilter.BF_BUFFER_SIZE];
            long read = xrefInputStream.read(buffer, ASBufferingInFilter.BF_BUFFER_SIZE);
            if (read == -1) {
                break;
            }
            buffer = ASBufferingInFilter.concatenate(remainedBytes, remainedBytes.length,
                    buffer, (int) read);

            int pointer = 0;
            COSXRefEntry xref;
            for (; objIdIndex < objIDs.size(); ++objIdIndex) {
                if(pointer + field0.length + field1.length + field2.length >
                        buffer.length) {
                    remainedBytes = Arrays.copyOfRange(buffer, pointer, (int) read);
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
    private long numberFromBytes(byte[] num) {
        long res = 0;
        for (int i = 0; i < num.length; ++i) {
            res += (num[i] & 0x00FF) << ((num.length - i - 1) * 8);
        }
        return res;
    }
}
