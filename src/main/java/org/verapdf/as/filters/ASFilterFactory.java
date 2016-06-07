package org.verapdf.as.filters;

import org.verapdf.as.ASAtom;
import org.verapdf.as.io.ASInputStream;
import org.verapdf.as.io.ASOutputStream;
import org.verapdf.cos.COSFilterFlateDecode;
import org.verapdf.cos.COSFilterFlateEncode;

/**
 * @author Sergey Shemyakov
 */
public class ASFilterFactory implements IASFilterFactory{

    private ASAtom filterType;

    public ASFilterFactory(ASAtom filterType) {
        this.filterType = filterType;
    }

    @Override
    public ASInFilter getInFilter(ASInputStream inputStream) {
        switch (filterType.get()) {
            case "ASCIIHexDecode":
                return new ASInFilter(inputStream);
            case "FlateDecode":
                return new COSFilterFlateDecode(inputStream);
            default:
                throw new IllegalArgumentException("Filter " + filterType.get() +
                        " is not supported.");
        }
    }

    @Override
    public ASOutFilter getOutFilter(ASOutputStream outputStream) {
        switch (filterType.get()) {
            case "ASCIIHexDecode":
                return new ASOutFilter(outputStream);
            case "FlateDecode":
                return new COSFilterFlateEncode(outputStream);
            default:
                throw new IllegalArgumentException("Filter " + filterType.get() +
                        " is not supported.");
        }
    }
}
