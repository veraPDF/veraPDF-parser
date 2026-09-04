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
package org.verapdf.pd.font.cff;

import org.junit.Test;
import org.verapdf.as.io.ASInputStream;
import org.verapdf.io.InternalInputStream;
import org.verapdf.tools.StaticResources;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Regression test for CID-keyed CFF Top DICT parsing.
 * <p>
 * Both fixtures are the same subset of Libertinus Serif (SIL OFL 1.1),
 * embedded by Typst as a CIDFontType0 with an Identity charset (cid == gid).
 * They differ only in Top DICT operator order:
 * <ul>
 *     <li>{@code LibertinusSerif-subset.cff}: ROS, Copyright, FontMatrix</li>
 *     <li>{@code LibertinusSerif-subset-ros-fontmatrix.cff}: ROS, FontMatrix, Copyright</li>
 * </ul>
 * The second ordering used to make the parser pick up the ROS Registry SID
 * (391) as FontMatrix[0], scaling every glyph width by 391000.
 */
public class CFFCIDFontProgramTest {

    private static final String RESOURCE_DIR = "src/test/resources/org/verapdf/pd/font/cff/";
    private static final String ROS_COPYRIGHT_FONTMATRIX = RESOURCE_DIR + "LibertinusSerif-subset.cff";
    private static final String ROS_FONTMATRIX_COPYRIGHT = RESOURCE_DIR + "LibertinusSerif-subset-ros-fontmatrix.cff";

    /**
     * Advance widths from the /W array of the CIDFont dictionary the fixtures
     * were embedded with, indexed by CID (== GID).
     */
    private static final float[] EXPECTED_WIDTHS = {
            500, 730, 447, 264, 504, 250, 747, 372, 505.99997f, 220, 316, 390, 271,
            542, 500, 541, 701, 485, 323, 695, 428, 310, 790, 457, 220
    };

    private static CFFCIDFontProgram parse(String path) throws IOException {
        StaticResources.clear();
        ASInputStream stream = new InternalInputStream(path, 2);
        CFFFontProgram font = new CFFFontProgram(stream, null, true);
        font.parseFont();
        assertTrue(font.isCIDFont());
        assertTrue(font.getFont() instanceof CFFCIDFontProgram);
        return (CFFCIDFontProgram) font.getFont();
    }

    private static void assertWidthsMatchFontDictionary(CFFCIDFontProgram font) {
        assertEquals(EXPECTED_WIDTHS.length, font.getNGlyphs());
        for (int gid = 0; gid < EXPECTED_WIDTHS.length; ++gid) {
            assertEquals("width of gid " + gid, EXPECTED_WIDTHS[gid], font.widths.getWidth(gid), 0.01f);
        }
    }

    @Test
    public void testWidthsWhenCopyrightSeparatesROSAndFontMatrix() throws IOException {
        assertWidthsMatchFontDictionary(parse(ROS_COPYRIGHT_FONTMATRIX));
    }

    @Test
    public void testWidthsWhenFontMatrixDirectlyFollowsROS() throws IOException {
        assertWidthsMatchFontDictionary(parse(ROS_FONTMATRIX_COPYRIGHT));
    }

    @Test
    public void testTopDictOperatorOrderDoesNotChangeWidths() throws IOException {
        CFFCIDFontProgram a = parse(ROS_COPYRIGHT_FONTMATRIX);
        CFFCIDFontProgram b = parse(ROS_FONTMATRIX_COPYRIGHT);
        assertEquals(a.getNGlyphs(), b.getNGlyphs());
        for (int gid = 0; gid < a.getNGlyphs(); ++gid) {
            assertEquals("width of gid " + gid, a.widths.getWidth(gid), b.widths.getWidth(gid), 0f);
        }
    }
}
