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

import org.junit.Assert;
import org.junit.Test;

import org.verapdf.cos.xref.COSXRefEntry;
import org.verapdf.cos.xref.COSXRefSection;
import java.io.IOException;

/**
 * @author Maxim Plushchov
 */
public class PDFParserTest {
    final private String xrefPath = "src/test/resources/org/verapdf/parser/xref";

    @Test
    public void testXrefParsing() {
        try {
            PDFParser pdfParser = new PDFParser(xrefPath);
            COSXRefSection section = new COSXRefSection();
            pdfParser.initializeToken();
            pdfParser.parseXrefTable(section);
            Assert.assertTrue(COSXRefEntry.FIRST_XREF_ENTRY.equals(section.getEntry(0)));
        } catch (IOException ex) {
            System.out.println("Parsing error: ");
            ex.printStackTrace();
        }
    }

}
