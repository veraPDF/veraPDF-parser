/**
 * This file is part of veraPDF Parser, a module of the veraPDF project.
 * Copyright (c) 2015-2024, veraPDF Consortium <info@verapdf.org>
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
package org.verapdf.cos.filters;

import org.junit.Test;
import org.verapdf.io.InternalInputStream;
import org.verapdf.io.InternalOutputStream;
import org.verapdf.pd.PDDocument;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

/**
 * @author Sergey Shemyakov
 */
public class COSFilterFlateEncodeTest {

    private static final String FILE_PATH =
            "src/test/resources/org/verapdf/cos/filters/validDocument.pdf";

    @Test
    public void test() throws IOException {

        byte[] toEncode = getDataToEncode();
        File encodedPDF = File.createTempFile("tmp_pdf_file", ".pdf");
        encodedPDF.deleteOnExit();
        encodePDF(toEncode, encodedPDF);
        InternalInputStream inputStream = new InternalInputStream(encodedPDF.getAbsolutePath());
        COSFilterFlateDecode decoder = new COSFilterFlateDecode(inputStream);
        PDDocument doc = new PDDocument(decoder);
        decoder.close();
        doc.close();
        encodedPDF.delete();
    }

    private byte[] getDataToEncode() throws IOException {
        byte[] file = new byte[20000];
        InternalInputStream stream = new InternalInputStream(FILE_PATH, 2);
        int length = stream.read(file, 20000);
        stream.close();
        return Arrays.copyOf(file, length);
    }

    private void encodePDF(byte[] toEncode, File encodedPDF) throws IOException {
        InternalOutputStream outputStream = new InternalOutputStream(encodedPDF.getAbsolutePath());
        COSFilterFlateEncode filter = new COSFilterFlateEncode(outputStream);
        filter.write(toEncode);
        filter.close();
    }
}
