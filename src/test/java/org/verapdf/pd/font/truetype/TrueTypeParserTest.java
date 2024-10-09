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
package org.verapdf.pd.font.truetype;

import org.junit.Test;
import org.verapdf.as.ASAtom;
import org.verapdf.cos.COSName;
import org.verapdf.cos.COSObject;
import org.verapdf.io.InternalInputStream;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

/**
 * @author Sergey Shemyakov
 */
public class TrueTypeParserTest {

    private static final String MONO_FONT_PATH = "src/test/resources/org/verapdf/pd/font/truetype/SourceCodePro-Bold.ttf";
    private static final String REGULAR_FONT_PATH = "src/test/resources/org/verapdf/pd/font/truetype/LiberationSans-Regular.ttf";
    private static final boolean IS_SYMBOLIC = false;
    private static final COSObject ENCODING = COSName.construct(ASAtom.MAC_ROMAN_ENCODING);

    @Test
    public void testMonospaced() throws IOException {

        TrueTypeFontProgram font = new TrueTypeFontProgram(new InternalInputStream(MONO_FONT_PATH, 2),
                IS_SYMBOLIC, ENCODING, null);
        font.parseFont();
        assertEquals(600f, font.getWidth("z"), 0.0);
        assertEquals(1000f, font.getWidth("yakute"), 0.0);
    }

    @Test
    public void testRegular() throws IOException {
        TrueTypeFontProgram font = new TrueTypeFontProgram(new InternalInputStream(REGULAR_FONT_PATH, 2),
                IS_SYMBOLIC, ENCODING, null);
        font.parseFont();
        assertEquals(500, (int) font.getWidth("z"));
        assertEquals(556, (int) font.getWidth("zero"));
        assertEquals(365, (int) font.getWidth("yakute"));
    }
}
