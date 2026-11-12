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
import org.verapdf.as.ASAtom;
import org.verapdf.cos.COSArray;
import org.verapdf.cos.COSDictionary;
import org.verapdf.cos.COSInteger;
import org.verapdf.cos.COSName;
import org.verapdf.cos.COSObject;

class PDType3FontUnicodeTest {

    @Test
    void nonStrictMappingFallsBackToPrintableCharacterCode() {
        PDType3Font font = type3FontWithCustomGlyphName();

        Assertions.assertEquals("A", font.toUnicode(65, false));
        Assertions.assertNull(font.toUnicode(65, true));
        Assertions.assertNull(font.toUnicode(31, false));
        Assertions.assertNull(font.toUnicode(127, false));
    }

    private static PDType3Font type3FontWithCustomGlyphName() {
        COSObject fontDictionary = COSDictionary.construct();

        COSObject encoding = COSDictionary.construct();
        COSObject differences = COSArray.construct();
        differences.add(COSInteger.construct(65));
        differences.add(COSName.construct(ASAtom.getASAtom("customGlyph")));
        encoding.setKey(ASAtom.DIFFERENCES, differences);
        fontDictionary.setKey(ASAtom.ENCODING, encoding);

        COSObject fontBBox = COSArray.construct();
        for (int coordinate : new int[]{0, 0, 1000, 1000}) {
            fontBBox.add(COSInteger.construct(coordinate));
        }
        fontDictionary.setKey(ASAtom.FONT_BBOX, fontBBox);

        return new PDType3Font((COSDictionary) fontDictionary.get());
    }
}
