package org.verapdf.parser;

import org.verapdf.as.ASAtom;
import org.verapdf.as.filters.EncodingPredictorDecode;
import org.verapdf.as.filters.EncodingPredictorResult;
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

        xrefInputStream = xrefCOSStream.getData(COSStream.FilterFlags.DECODE);
        fieldSizes = (COSArray) xrefCOSStream.getKey(ASAtom.W).get();
        if (fieldSizes.size() != 3) {
            throw new IOException("W array in xref should have 3 elements.");
        }
        initializeIndex();
        initializeObjIDs();
        parseStream();
        EncodingPredictorDecode.resetPreviousLine();
        setTrailer();
    }

    /**
     * This method makes sure that Index array is correctly initialized.
     *
     * @throws IOException
     */
    private void initializeIndex()
            throws IOException {
        index = (COSArray) xrefCOSStream.getKey(ASAtom.INDEX).get();

        if (index == null) {
            COSObject[] defaultIndex = new COSObject[2];
            defaultIndex[0] = COSInteger.construct(0);
            defaultIndex[1] = xrefCOSStream.getKey(ASAtom.SIZE);
            index = (COSArray) COSArray.construct(2, defaultIndex).get();
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
            COSInteger firstID = (COSInteger) index.at(2 * i).get();
            COSInteger lengthOfSubsection = (COSInteger) index.at(2 * i + 1).get();
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
        byte[] field0 = new byte[(int) fieldSizes.at(0).getInteger()];
        byte[] field1 = new byte[(int) fieldSizes.at(1).getInteger()];
        byte[] field2 = new byte[(int) fieldSizes.at(2).getInteger()];
        byte[] buffer;
        byte[] decodedStream = new byte[0];
        byte[] unpredictedBytes = new byte[0];
        int objIdIndex = 0;

        while (true) {
            buffer = new byte[ASBufferingInFilter.BF_BUFFER_SIZE];
            long read = xrefInputStream.read(buffer, ASBufferingInFilter.BF_BUFFER_SIZE);   //reading new portion of deflated unpredicted data
            if (read == -1) {
                break;
            }
            unpredictedBytes = ASBufferingInFilter.concatenate(unpredictedBytes, unpredictedBytes.length,
                    buffer, (int) read);    //adding unpredicted data from previous step to the beginning
            EncodingPredictorResult res = getPredictorResult(unpredictedBytes); //applying Predictor to the whole buffer of unpredicted data
            unpredictedBytes = res.getUnpredictedData();    //remembering unpredicted bytes
            buffer = res.getPredictedData();    //getting data that can be processed
            decodedStream = ASBufferingInFilter.concatenate(decodedStream, decodedStream.length,
                    buffer, buffer.length); //adding predicted data from previous iteration

            int pointer = 0;
            COSXRefEntry xref;
            for (; objIdIndex < objIDs.size(); ++objIdIndex) {
                if(pointer + field0.length + field1.length + field2.length >
                        decodedStream.length) {
                    decodedStream = Arrays.copyOfRange(decodedStream, pointer,
                            decodedStream.length);
                    break;
                }
                Long id = objIDs.get(objIdIndex);
                System.arraycopy(decodedStream, pointer, field0, 0, field0.length);
                pointer += field0.length;
                System.arraycopy(decodedStream, pointer, field1, 0, field1.length);
                pointer += field1.length;
                System.arraycopy(decodedStream, pointer, field2, 0, field2.length);
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

    /**
     * This helper method applies predictor to given byte array in a way
     * determined by /DecodeParams of xref COSStream dictionary.
     *
     * @param data byte array for which predictor should be applied.
     * @return byte array after predictor processing.
     */
    private EncodingPredictorResult getPredictorResult(byte[] data) throws IOException {  // TODO: process case of multiple filters
        //default values
        int predictor,
                colors = 1,
                bitsPerComponent = 8,
                columns = 1;    // TODO: EarlyChange?

        COSBase decodeParams = xrefCOSStream.getKey(ASAtom.DECODE_PARMS).get();
        if (decodeParams.getType().equals(COSObjType.COSDictT)) {   // DecodeParams can be array or dict
            if (decodeParams.knownKey(ASAtom.PREDICTOR)) {
                predictor = decodeParams.getIntegerKey(ASAtom.PREDICTOR).intValue();
            } else {
                return new EncodingPredictorResult(data, new byte[0]);
            }
            if (predictor == 1) {
                return new EncodingPredictorResult(data, new byte[0]);
            }
            if (decodeParams.knownKey(ASAtom.COLORS)) {
                colors = decodeParams.getIntegerKey(ASAtom.COLORS).intValue();
            }
            if (decodeParams.knownKey(ASAtom.BITS_PER_COMPONENT)) {
                bitsPerComponent = decodeParams.getIntegerKey(ASAtom.BITS_PER_COMPONENT).intValue();
            }
            if (decodeParams.knownKey(ASAtom.COLUMNS)) {
                columns = decodeParams.getIntegerKey(ASAtom.COLUMNS).intValue();
            }
        } else {
            throw new RuntimeException("Case when DecodeParams of xref is " +
                    decodeParams.getType() + "in not supported yet.");
        }
        return EncodingPredictorDecode.decodePredictor(predictor, colors,
                bitsPerComponent, columns, data);
    }
}
