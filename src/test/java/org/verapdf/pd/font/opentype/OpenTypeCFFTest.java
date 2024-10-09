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
package org.verapdf.pd.font.opentype;

import org.junit.Test;
import org.verapdf.as.ASAtom;
import org.verapdf.as.io.ASInputStream;
import org.verapdf.cos.COSName;
import org.verapdf.cos.COSObject;
import org.verapdf.io.InternalInputStream;
import org.verapdf.pd.font.cff.CFFFontProgram;

import java.io.IOException;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author Sergey Shemyakov
 */
public class OpenTypeCFFTest {

    private final String fontFilePath = "src/test/resources/org/verapdf/pd/font/opentype/ShortStack-Regular.otf";

    @Test
    public void test() throws IOException {
        COSObject encoding = COSName.construct(ASAtom.WIN_ANSI_ENCODING);
        ASInputStream stream = new InternalInputStream(fontFilePath, 2);
        OpenTypeFontProgram font = new OpenTypeFontProgram(stream, true, false, false,
                encoding, null, true, null, null);
        font.parseFont();
        assertTrue(font.getFont() instanceof CFFFontProgram);
        assertFalse(((CFFFontProgram) font.getFont()).isCIDFont());
    }

}
