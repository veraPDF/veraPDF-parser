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
package org.verapdf.cos.visitor;

import org.junit.Assert;
import org.junit.Test;
import org.verapdf.pd.PDDocument;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Checks that a trailer written out never advertises a cross-reference stream:
 * the writer only emits classic cross-reference tables, so a /XRefStm inherited
 * from the source trailer would point at a section it did not write.
 */
public class WriterTest {

    /**
     * Guards the fixture itself: without /XRefStm in its last trailer the other
     * tests would pass for the wrong reason.
     */
    @Test
    public void testFixtureIsHybridReferenceFile() throws IOException {
        PDDocument document = new PDDocument(new ByteArrayInputStream(hybridReferencePDF()));
        try {
            Assert.assertNotNull(document.getDocument().getTrailer().getXRefStm());
            Assert.assertEquals(1, document.getPages().size());
        } finally {
            document.close();
        }
    }

    @Test
    public void testRewrittenTrailerHasNoXRefStm() throws IOException {
        Path source = createTempFile("hybrid", hybridReferencePDF());
        Path target = Files.createTempFile("rewritten", ".pdf");
        try {
            PDDocument document = new PDDocument(source.toString());
            try {
                document.saveAs(target.toString());
            } finally {
                document.close();
            }
            PDDocument rewritten = new PDDocument(target.toString());
            try {
                Assert.assertNull(rewritten.getDocument().getTrailer().getXRefStm());
                Assert.assertEquals(1, rewritten.getPages().size());
            } finally {
                rewritten.close();
            }
        } finally {
            Files.deleteIfExists(source);
            Files.deleteIfExists(target);
        }
    }

    @Test
    public void testIncrementallyUpdatedTrailerHasNoXRefStm() throws IOException {
        Path source = createTempFile("hybrid", hybridReferencePDF());
        try {
            ByteArrayOutputStream updated = new ByteArrayOutputStream();
            PDDocument document = new PDDocument(source.toString());
            try {
                document.saveTo(updated);
            } finally {
                document.close();
            }
            PDDocument reopened = new PDDocument(new ByteArrayInputStream(updated.toByteArray()));
            try {
                Assert.assertNull(reopened.getDocument().getTrailer().getXRefStm());
                Assert.assertEquals(1, reopened.getPages().size());
            } finally {
                reopened.close();
            }
        } finally {
            Files.deleteIfExists(source);
        }
    }

    private static Path createTempFile(String prefix, byte[] content) throws IOException {
        Path file = Files.createTempFile(prefix, ".pdf");
        Files.write(file, content);
        return file;
    }

    /**
     * Builds a two revision hybrid-reference file as described in ISO 32000-1,
     * 7.5.8.4: the last classic section is empty and its trailer delegates to a
     * cross-reference stream through /XRefStm. This is what word processors
     * commonly produce, and such a file is valid.
     */
    private static byte[] hybridReferencePDF() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        long[] offsets = new long[7];

        write(out, "%PDF-1.7\n%âãÏÓ\n");

        offsets[1] = out.size();
        write(out, "1 0 obj\n<</Type /Catalog /Pages 2 0 R>>\nendobj\n");
        offsets[2] = out.size();
        write(out, "2 0 obj\n<</Type /Pages /Kids [3 0 R] /Count 1>>\nendobj\n");
        offsets[3] = out.size();
        write(out, "3 0 obj\n<</Type /Page /Parent 2 0 R /MediaBox [0 0 200 200]"
                + " /Contents 4 0 R /Resources <</Font <</F1 5 0 R>>>>>>\nendobj\n");
        String contents = "BT /F1 14 Tf 20 150 Td (Hybrid reference fixture) Tj ET\n";
        offsets[4] = out.size();
        write(out, "4 0 obj\n<</Length " + contents.length() + ">>\nstream\n" + contents
                + "endstream\nendobj\n");
        offsets[5] = out.size();
        write(out, "5 0 obj\n<</Type /Font /Subtype /Type1 /BaseFont /Helvetica>>\nendobj\n");

        long firstXRef = out.size();
        StringBuilder table = new StringBuilder("xref\n0 6\n0000000000 65535 f \n");
        for (int i = 1; i <= 5; i++) {
            table.append(String.format("%010d 00000 n \n", offsets[i]));
        }
        write(out, table.toString());
        write(out, "trailer\n<</Size 6 /Root 1 0 R>>\nstartxref\n" + firstXRef + "\n%%EOF\n");

        offsets[6] = out.size();
        byte[] entries = xRefStreamEntries(offsets);
        write(out, "6 0 obj\n<</Type /XRef /Size 7 /W [1 4 2] /Index [0 7] /Root 1 0 R /Length "
                + entries.length + ">>\nstream\n");
        out.write(entries, 0, entries.length);
        write(out, "\nendstream\nendobj\n");

        long secondXRef = out.size();
        write(out, "xref\n0 0\ntrailer\n<</Size 7 /Root 1 0 R /Prev " + firstXRef + " /XRefStm "
                + offsets[6] + ">>\nstartxref\n" + secondXRef + "\n%%EOF\n");

        return out.toByteArray();
    }

    private static byte[] xRefStreamEntries(long[] offsets) {
        ByteArrayOutputStream entries = new ByteArrayOutputStream();
        for (int i = 0; i < 7; i++) {
            entries.write(i == 0 ? 0 : 1);
            long offset = i == 0 ? 0 : offsets[i];
            entries.write((int) ((offset >>> 24) & 0xFF));
            entries.write((int) ((offset >>> 16) & 0xFF));
            entries.write((int) ((offset >>> 8) & 0xFF));
            entries.write((int) (offset & 0xFF));
            int generation = i == 0 ? 65535 : 0;
            entries.write((generation >>> 8) & 0xFF);
            entries.write(generation & 0xFF);
        }
        return entries.toByteArray();
    }

    private static void write(ByteArrayOutputStream out, String value) {
        byte[] bytes = value.getBytes(StandardCharsets.ISO_8859_1);
        out.write(bytes, 0, bytes.length);
    }
}
