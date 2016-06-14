package org.verapdf.io;

import org.verapdf.as.ASAtom;
import org.verapdf.as.io.ASInputStream;
import org.verapdf.cos.COSBase;
import org.verapdf.cos.COSObjType;
import org.verapdf.cos.COSStream;
import org.verapdf.cos.EncodingPredictor;
import org.verapdf.parser.XrefStreamParser;

import java.io.ByteArrayInputStream;
import java.io.IOException;

/**
 * This class helps to store decoded object streams in memory. It does not
 * provide any functionality to parse object streams.
 * @author Sergey Shemyakov
 */
public class SavedDecodedObjectStream {

    private static final int DEFAULT_BUFFER_SIZE = 2048;    //TODO: duplicating code, DO SOMETHING!!!

    private byte [] decodedStream;  //TODO: not the best way of storing data?

    /**
     * Constructor from object stream.
     * @param objectStream
     */
    public SavedDecodedObjectStream(COSStream objectStream) throws IOException {
        ASInputStream deflated = objectStream.getData(COSStream.FilterFlags.DECODE);
        byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
        decodedStream = new byte[0];
        while (true) {
            long read = deflated.read(buffer, (long) buffer.length);
            if (read == 0) {
                break;
            }
            decodedStream = XrefStreamParser.concatenate(decodedStream, buffer, (int) read);
        }
        decodedStream = getPredictorResult(decodedStream, objectStream);
    }

    public byte[] getDecodedStream() {
        return decodedStream;
    }

//----------------------------------------------------------------------------------------------------------//TODO: duplicating code, DO SOMETHING!!!
    /**
     * This helper method applies predictor to given byte array in a way
     * determined by /DecodeParams of xref COSStream dictionary.
     *
     * @param data byte array for which predictor should be applied.
     * @return byte array after predictor processing.
     */
    private byte[] getPredictorResult(byte[] data, COSStream xrefCOSStream) {
        //default values
        int predictor,
                colors = 1,
                bitsPerComponent = 8,
                columns = 1;

        COSBase decodeParams = xrefCOSStream.getKey(ASAtom.DECODE_PARMS).get();
        if (decodeParams.getType().equals(COSObjType.COSDictT)) {   // DecodeParams can be array or dict
            if (decodeParams.knownKey(ASAtom.PREDICTOR)) {
                predictor = decodeParams.getIntegerKey(ASAtom.PREDICTOR).intValue();
            } else {
                return data;
            }
            if (predictor == 1) {
                return data;
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
        return EncodingPredictor.decodePredictor(predictor, colors,
                bitsPerComponent, columns, data);
    }
}
