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
package org.verapdf.factory.fonts;

import org.verapdf.as.ASAtom;
import org.verapdf.cos.COSDictionary;
import org.verapdf.cos.COSObjType;
import org.verapdf.cos.COSObject;
import org.verapdf.pd.font.PDFont;
import org.verapdf.pd.font.PDType0Font;
import org.verapdf.pd.font.truetype.PDTrueTypeFont;
import org.verapdf.pd.font.type1.PDType1Font;
import org.verapdf.pd.font.type3.PDType3Font;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Creates PDFont from COSObject that is font dictionary.
 *
 * @author Sergey Shemyakov
 */
public class PDFontFactory {

    private static final Logger LOGGER = Logger.getLogger(PDFontFactory.class.getCanonicalName());

    private PDFontFactory() {
    }

    /**
     * @param fontDictionary is COSObject that contains font dictionary.
     * @return PDFont that corresponds to this dictionary.
     */
    public static PDFont getPDFont(COSObject fontDictionary) {
        if (fontDictionary != null && !fontDictionary.empty()) {
            if (fontDictionary.getType() == COSObjType.COS_DICT) {
                COSDictionary dict = (COSDictionary) fontDictionary.getDirectBase();
                ASAtom subtype = fontDictionary.getNameKey(ASAtom.SUBTYPE);
                if (subtype == ASAtom.TYPE1 ||
                        subtype == ASAtom.MM_TYPE1) {
                    return new PDType1Font(dict);
                } else if (subtype == ASAtom.TRUE_TYPE) {
                    return new PDTrueTypeFont(dict);
                } else if (subtype == ASAtom.TYPE3) {
                    return new PDType3Font(dict);
                } else if (subtype == ASAtom.TYPE0) {
                    return new PDType0Font(dict);
                } else {
                    return null;
                }
            }
            LOGGER.log(Level.SEVERE, "Font object is not a dictionary");
        }
		return null;
    }
}
