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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.verapdf.as.ASAtom;
import org.verapdf.exceptions.ResourceLimitExceededException;
import org.verapdf.pd.PDDocument;

import java.io.IOException;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;

public class COSDocumentObjectLimitTest {

    private static final String FILE_PATH = "src/test/resources/org/verapdf/cos/filters/validDocument.pdf";

    private int previousLimit;

    @Before
    public void rememberLimit() {
        previousLimit = COSDocument.getMaxNumberOfObjects();
    }

    @After
    public void restoreLimit() {
        COSDocument.setMaxNumberOfObjects(previousLimit);
    }

    @Test
    public void shouldEnumerateObjectsWhenTheLimitIsNotExceeded() throws IOException {
        PDDocument document = new PDDocument(FILE_PATH);
        try {
            COSDocument.setMaxNumberOfObjects(Integer.MAX_VALUE);
            assertFalse(document.getDocument().getObjects().isEmpty());
        } finally {
            document.close();
        }
    }

    @Test
    public void shouldThrowResourceLimitExceededExceptionWhenTheLimitIsExceeded() throws IOException {
        PDDocument document = new PDDocument(FILE_PATH);
        try {
            COSDocument cosDocument = document.getDocument();
            COSDocument.setMaxNumberOfObjects(1);
            assertThrows(ResourceLimitExceededException.class, cosDocument::getObjects);
            assertThrows(ResourceLimitExceededException.class, cosDocument::getObjectsMap);
            assertThrows(ResourceLimitExceededException.class, () -> cosDocument.getObjectsByType(ASAtom.PAGE));
        } finally {
            document.close();
        }
    }
}
