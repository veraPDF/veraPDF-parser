package org.verapdf.as.filters;

import org.verapdf.as.ASAtom;
import org.verapdf.as.filters.io.ASBufferingInFilter;
import org.verapdf.as.filters.io.ASBufferingOutFilter;
import org.verapdf.as.io.ASInputStream;
import org.verapdf.as.io.ASOutputStream;
import org.verapdf.cos.COSFilterFlateDecode;
import org.verapdf.cos.COSFilterFlateEncode;

import java.io.IOError;
import java.io.IOException;

/**
 * @author Sergey Shemyakov
 */
public class ASFilterFactory implements IASFilterFactory{

    private ASAtom filterType;

    public ASFilterFactory(ASAtom filterType) {
        this.filterType = filterType;
    }

    /**
     * Gets decoded stream from the given one.
     * @param inputStream is an encoded stream.
     * @return decoded stream.
     * @throws IOException if decode filter for given stream is not supported.
     */
    @Override
    public ASInFilter getInFilter(ASInputStream inputStream) throws IOException {   //TODO: decode params
        switch (filterType.get()) {
            case "ASCIIHexDecode":
                return new ASBufferingInFilter(inputStream);
            case "FlateDecode":
                return new COSFilterFlateDecode(inputStream);
            default:
                throw new IOException("Filter " + filterType.get() +
                        " is not supported.");
        }
    }

    /**
     * Gets encoded stream from the given one.
     * @param outputStream is data to be encoded.
     * @return encoded stream.
     * @throws IOException if current encode filter is not supported.
     */
    @Override
    public ASOutFilter getOutFilter(ASOutputStream outputStream) throws IOException {
        switch (filterType.get()) {
            case "ASCIIHexDecode":
                return new ASBufferingOutFilter(outputStream);
            case "FlateDecode":
                return new COSFilterFlateEncode(outputStream);
            default:
                throw new IOException("Filter " + filterType.get() +
                        " is not supported.");
        }
    }
}
