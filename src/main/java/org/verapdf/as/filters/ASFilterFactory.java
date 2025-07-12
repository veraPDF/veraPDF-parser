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
package org.verapdf.as.filters;

import org.verapdf.as.ASAtom;
import org.verapdf.as.filters.io.ASBufferingOutFilter;
import org.verapdf.as.io.ASInputStream;
import org.verapdf.as.io.ASOutputStream;
import org.verapdf.cos.COSDictionary;
import org.verapdf.cos.filters.*;

import java.io.IOException;

/**
 * Factory for constructing COSFilters.
 *
 * @author Sergey Shemyakov
 */
public class ASFilterFactory implements IASFilterFactory{

    private final ASAtom filterType;

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
    public ASInFilter getInFilter(ASInputStream inputStream,
                                  COSDictionary decodeParams) throws IOException {
        switch (filterType.getValue()) {
            case "ASCIIHexDecode":
                return new COSFilterASCIIHexDecode(inputStream);
            case "RunLengthDecode":
                return new RunLengthDecode(inputStream);
            case "FlateDecode":
                return new COSPredictorDecode(new COSFilterFlateDecode(inputStream), decodeParams);
            case "ASCII85Decode":
                return new COSFilterASCII85Decode(inputStream);
            case "LZWDecode":
                return new COSPredictorDecode(new COSFilterLZWDecode(inputStream,
                        decodeParams), decodeParams);
            default:
                throw new IOException("Filter " + filterType.getValue() +
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
        switch (filterType.getValue()) {
            case "ASCIIHexDecode":
                return new ASBufferingOutFilter(outputStream);
            case "FlateDecode":
                return new COSFilterFlateEncode(outputStream);
            default:
                throw new IOException("Filter " + filterType.getValue() +
                        " is not supported.");
        }
    }
}
