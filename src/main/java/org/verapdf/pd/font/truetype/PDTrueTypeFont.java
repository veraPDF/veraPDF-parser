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
package org.verapdf.pd.font.truetype;

import org.verapdf.as.ASAtom;
import org.verapdf.as.io.ASInputStream;
import org.verapdf.cos.COSDictionary;
import org.verapdf.cos.COSKey;
import org.verapdf.cos.COSObject;
import org.verapdf.cos.COSStream;
import org.verapdf.pd.font.FontProgram;
import org.verapdf.pd.font.PDSimpleFont;
import org.verapdf.pd.font.opentype.OpenTypeFontProgram;
import org.verapdf.tools.FontProgramIDGenerator;
import org.verapdf.tools.StaticResources;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Represents True Type font on PD level.
 *
 * @author Sergey Shemyakov
 */
public class PDTrueTypeFont extends PDSimpleFont {

    private static final Logger LOGGER = Logger.getLogger(PDTrueTypeFont.class.getCanonicalName());

    /**
     * Constructor from true type font dictionary.
     * @param dictionary is true type font dictionary.
     */
    public PDTrueTypeFont(COSDictionary dictionary) {
        super(dictionary);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public FontProgram getFontProgram() {
        if (!this.isFontParsed) {
            this.isFontParsed = true;
            if (fontDescriptor.canParseFontFile(ASAtom.FONT_FILE2)) {
                parseTrueTypeFontProgram(ASAtom.FONT_FILE2);
            } else if (fontDescriptor.canParseFontFile(ASAtom.FONT_FILE3)) {
                parseTrueTypeFontProgram(ASAtom.FONT_FILE3);
            } else {
                this.fontProgram = null;
            }
        }
        return this.fontProgram;
    }

    private void parseTrueTypeFontProgram(ASAtom fontFileType) {
        COSStream trueTypeFontFile = null;
        if (fontFileType == ASAtom.FONT_FILE2) {
            trueTypeFontFile = fontDescriptor.getFontFile2();
        } else if (fontFileType == ASAtom.FONT_FILE3) {
            trueTypeFontFile = fontDescriptor.getFontFile3();
        }
        if (trueTypeFontFile != null) {
            COSKey key = trueTypeFontFile.getObjectKey();
            boolean isSymbolic = this.isSymbolic();
            COSObject encoding = this.getEncoding();
            try {
                if (fontFileType == ASAtom.FONT_FILE2) {
                    String fontProgramID = FontProgramIDGenerator.getTrueTypeFontProgramID(key, isSymbolic, encoding);
                    this.fontProgram = StaticResources.getCachedFont(fontProgramID);
                    if (fontProgram == null) {
                        try (ASInputStream fontData = trueTypeFontFile.getData(COSStream.FilterFlags.DECODE)) {
                            this.fontProgram = new TrueTypeFontProgram(fontData, isSymbolic,
                                    encoding);
                            StaticResources.cacheFontProgram(fontProgramID, this.fontProgram);
                        }
                    }
                } else {    // fontFile3
                    boolean isSubset = this.isSubset();
                    String fontProgramID = FontProgramIDGenerator.getOpenTypeFontProgramID(key, false, isSymbolic, encoding, null, isSubset);
                    this.fontProgram = StaticResources.getCachedFont(fontProgramID);
                    if (fontProgram == null) {
                        try (ASInputStream fontData = trueTypeFontFile.getData(COSStream.FilterFlags.DECODE)) {
                            this.fontProgram = new OpenTypeFontProgram(fontData, false,
                                    isSymbolic, encoding, null, isSubset);
                        }
                    }
                }
            } catch (IOException e) {
                LOGGER.log(Level.FINE, "Can't read TrueType font program.", e);
            }
        }
    }

    @Override
    public float getWidthFromProgram(int code) {
        return this.getFontProgram().getWidth(code);
    }

    @Override
    public boolean glyphIsPresent(int code) {
        return this.getFontProgram().containsCode(code);
    }
}
