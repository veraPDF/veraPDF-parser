package org.verapdf.io;

import org.verapdf.as.ASAtom;
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
 * @author Sergey Shemyakov
 */
public class XrefStreamParser {

    private static COSArray index;
    private static ASInputStream xrefInputStream;
    private static COSArray fieldSizes;
    private static List<Long> objIDs;

    /**
     * Empty constructor that does nothing.
     */
    public XrefStreamParser() {
    }

    /**
     * This is an entry point for parsing xref stream and trailer.
     * @param section is xref section, where xref entries and trailer
     *                information will be written.
     * @param xrefCOSStream is xref COSStream.
     * @throws IOException
     */
    public static void parseStreamAndTrailer(COSXRefInfo section,
                                             COSStream xrefCOSStream) throws IOException {

        xrefInputStream = xrefCOSStream.getData(COSStream.FilterFlags.DECODE);
        fieldSizes = (COSArray) xrefCOSStream.getKey(ASAtom.W).get();
        if (fieldSizes.size() != 3) {
            throw new IOException("W array in xref stream has not 3 elements."); //TODO: what to do with exceptions?
        }
        initializeIndex(xrefCOSStream);
        initializeObjIDs();
        parseStream(section);
        setTrailer(section, xrefCOSStream);
    }

    private static void initializeIndex(COSStream xrefCOSStream)
            throws IOException {
        index = (COSArray) xrefCOSStream.getKey(ASAtom.INDEX).get();

        if (index == null) {
            COSObject[] defaultIndex = new COSObject[2];
            defaultIndex[0] = COSInteger.construct(0);
            defaultIndex[1] = xrefCOSStream.getKey(ASAtom.SIZE);
            index = (COSArray) COSArray.construct(2, defaultIndex).get();
        } else if (index.size() % 2 != 0) {
            throw new IOException("Index array in xref stream has odd amount of elements.");//TODO: what to do with exceptions?
        }
    }

    private static void initializeObjIDs() {
        objIDs = new ArrayList<>();
        for (int i = 0; i < index.size() / 2; ++i) {
            COSInteger firstID = (COSInteger) index.at(2 * i).get();
            COSInteger lengthOfSubsection = (COSInteger) index.at(2 * i + 1).get();
            for (int j = 0; j < lengthOfSubsection.get(); ++j) {
                objIDs.add(firstID.get() + j);
            }
        }
    }

    private static void parseStream(COSXRefInfo section) throws IOException {
        byte[] field0 = new byte[(int) fieldSizes.at(0).getInteger()];
        byte[] field1 = new byte[(int) fieldSizes.at(1).getInteger()];
        byte[] field2 = new byte[(int) fieldSizes.at(2).getInteger()];
        byte[] buffer = new byte[2048]; //TODO: 2048 can be not enough for some streams
        xrefInputStream.read(buffer, 2048);
        int pointer = 0;

        COSXRefEntry xref;
        for (Long id : objIDs) {    // TODO: obtained bytes are not the final decoded bytes somehow. See org.apache.pdfbox.filter.Predictor for
            field0 = Arrays.copyOfRange(buffer, pointer, pointer + field0.length);  //TODO: information what should be done + consult Boris.
            pointer += field0.length;
            field1 = Arrays.copyOfRange(buffer, pointer, pointer + field1.length);
            pointer += field1.length;
            field2 = Arrays.copyOfRange(buffer, pointer, pointer + field2.length);
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
                        xref.generation = (int) numberFromBytes(field2); // If object is written into stream then generation indicates
                    }                                               // index of object within stream. Generation number shall be implicitly 0.
                    section.getXRefSection().add(new COSKey(id.intValue(),
                            xref.generation), xref.offset);
                    break;
                default:
                    throw new IOException("Error in parsing xref stream");  //TODO: what to do with exceptions?
            }
        }
    }

    private static void setTrailer(COSXRefInfo section, COSStream xrefCOSStream) {  // TODO: anything else?
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

    private static long numberFromBytes(byte[] num) {
        long res = 0;
        for (int i = 0; i < num.length; ++i) {
            res += (num[i] & 0x00FF) << ((num.length - i - 1) * 8);
        }
        return res;
    }
}
