/*
 * This file is part of veraPDF Parser, a module of the veraPDF project.
 * Copyright (c) 2015-2026, veraPDF Consortium <info@verapdf.org>
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
package org.verapdf.cos;

import org.junit.Test;
import org.verapdf.pd.PDDocument;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.Assert.assertEquals;

public class COSDocumentPostEOFDataSizeTest {

    private static final String FILE_PATH = "src/test/resources/org/verapdf/cos/filters/validDocument.pdf";

    @Test
    public void shouldReportPostEOFDataSizeGreaterThanByteRange() throws IOException {
        byte[] pdf = Files.readAllBytes(Paths.get(FILE_PATH));
        int trailingBytes = 256;
        ByteArrayOutputStream paddedPdf = new ByteArrayOutputStream(pdf.length + trailingBytes);
        paddedPdf.write(pdf);
        paddedPdf.write(new byte[trailingBytes]);

        PDDocument document = new PDDocument(new ByteArrayInputStream(paddedPdf.toByteArray()));
        try {
            assertEquals(trailingBytes, document.getDocument().getPostEOFDataSize());
        } finally {
            document.close();
        }
    }

    @Test
    public void shouldReportLargePostEOFDataSizeExactly() throws IOException {
        byte[] pdf = Files.readAllBytes(Paths.get(FILE_PATH));
        int trailingBytes = 500;
        ByteArrayOutputStream paddedPdf = new ByteArrayOutputStream(pdf.length + trailingBytes);
        paddedPdf.write(pdf);
        paddedPdf.write(new byte[trailingBytes]);

        PDDocument document = new PDDocument(new ByteArrayInputStream(paddedPdf.toByteArray()));
        try {
            assertEquals(trailingBytes, document.getDocument().getPostEOFDataSize());
        } finally {
            document.close();
        }
    }
}
