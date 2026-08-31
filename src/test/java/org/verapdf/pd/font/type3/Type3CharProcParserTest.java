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
package org.verapdf.pd.font.type3;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.verapdf.as.io.ASMemoryInStream;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

class Type3CharProcParserTest {

    private static final double EPSILON = 1.0e-6;

    private static Type3CharProcParser parse(String charProc) throws IOException {
        Type3CharProcParser parser = new Type3CharProcParser(
                new ASMemoryInStream(charProc.getBytes(StandardCharsets.ISO_8859_1)));
        parser.parse();
        return parser;
    }

    /**
     * A char proc that starts with d1 declares a glyph bounding box, so both
     * metrics are available.
     */
    @Test
    void d1CharProcReportsMetrics() throws IOException {
        try (Type3CharProcParser parser = parse("1000 0 0 -200 750 800 d1\n")) {
            Assertions.assertEquals(1000, parser.getWidth(), EPSILON);
            Assertions.assertEquals(800, parser.getAscentOrNull(), EPSILON);
            Assertions.assertEquals(-200, parser.getDescentOrNull(), EPSILON);
        }
    }

    /**
     * A char proc that starts with d0 specifies the glyph width only
     * (ISO 32000-1, 9.6.5.3). Its ascent and descent are undefined, so they
     * must be reported as null rather than defaulted to zero: a zero descent
     * would win a minimum comparison against the real descents of the other
     * glyphs in the same font.
     */
    @Test
    void d0CharProcReportsNullMetrics() throws IOException {
        try (Type3CharProcParser parser = parse("1000 0 d0\n")) {
            Assertions.assertEquals(1000, parser.getWidth(), EPSILON);
            Assertions.assertNull(parser.getAscentOrNull());
            Assertions.assertNull(parser.getDescentOrNull());
        }
    }

    /**
     * The pre-existing primitive getters keep their released signatures and
     * substitute 0 for an undefined metric instead of throwing.
     */
    @Test
    @SuppressWarnings("deprecation")
    void deprecatedPrimitiveGettersSubstituteZeroForUndefinedMetrics() throws IOException {
        try (Type3CharProcParser parser = parse("1000 0 d0\n")) {
            Assertions.assertEquals(0, parser.getAscent(), EPSILON);
            Assertions.assertEquals(0, parser.getDescent(), EPSILON);
        }
        try (Type3CharProcParser parser = parse("1000 0 0 -200 750 800 d1\n")) {
            Assertions.assertEquals(800, parser.getAscent(), EPSILON);
            Assertions.assertEquals(-200, parser.getDescent(), EPSILON);
        }
    }

    /**
     * A char proc whose bounding box is not terminated by d1 is corrupted;
     * parse() reports it and leaves no metrics behind.
     */
    @Test
    void corruptedCharProcThrowsAndClearsMetrics() throws IOException {
        Type3CharProcParser parser = new Type3CharProcParser(
                new ASMemoryInStream("1000 0 0 -200 750 800 xx\n".getBytes(StandardCharsets.ISO_8859_1)));
        try (Type3CharProcParser closeable = parser) {
            Assertions.assertThrows(IOException.class, closeable::parse);
        }
        Assertions.assertEquals(-1, parser.getWidth(), EPSILON);
        Assertions.assertNull(parser.getAscentOrNull());
        Assertions.assertNull(parser.getDescentOrNull());
    }
}
