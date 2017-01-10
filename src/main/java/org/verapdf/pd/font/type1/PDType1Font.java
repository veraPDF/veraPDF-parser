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
package org.verapdf.pd.font.type1;

import org.verapdf.as.ASAtom;
import org.verapdf.as.io.ASInputStream;
import org.verapdf.as.io.ASMemoryInStream;
import org.verapdf.cos.COSDictionary;
import org.verapdf.cos.COSObjType;
import org.verapdf.cos.COSObject;
import org.verapdf.cos.COSStream;
import org.verapdf.parser.COSParser;
import org.verapdf.pd.font.FontProgram;
import org.verapdf.pd.font.PDSimpleFont;
import org.verapdf.pd.font.cff.CFFFontProgram;
import org.verapdf.pd.font.opentype.OpenTypeFontProgram;
import org.verapdf.pd.font.truetype.TrueTypePredefined;

import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Sergey Shemyakov
 */
public class PDType1Font extends PDSimpleFont {

    private static final Logger LOGGER = Logger.getLogger(PDType1Font.class.getCanonicalName());
    public static final ASAtom[] STANDARD_FONT_NAMES = {
            ASAtom.COURIER_BOLD,
            ASAtom.COURIER_BOLD_OBLIQUE,
            ASAtom.COURIER,
            ASAtom.COURIER_OBLIQUE,
            ASAtom.HELVETICA,
            ASAtom.HELVETICA_BOLD,
            ASAtom.HELVETICA_BOLD_OBLIQUE,
            ASAtom.HELVETICA_OBLIQUE,
            ASAtom.SYMBOL,
            ASAtom.TIMES_BOLD,
            ASAtom.TIMES_BOLD_ITALIC,
            ASAtom.TIMES_ITALIC,
            ASAtom.TIMES_ROMAN,
            ASAtom.ZAPF_DINGBATS};

    private Boolean isStandard = null;

    public PDType1Font(COSDictionary dictionary) {
        super(dictionary);
    }

    public Set<String> getDescriptorCharSet() {
        String descriptorCharSetString = this.fontDescriptor.getCharSet();
        if (descriptorCharSetString != null) {
            try {
                ASMemoryInStream stream =
                        new ASMemoryInStream(descriptorCharSetString.getBytes());
                Set<String> descriptorCharSet = new TreeSet<>();
                COSParser parser = new COSParser(stream);
                COSObject glyphName = parser.nextObject();
                while (!glyphName.empty()) {
                    if (glyphName.getType() == COSObjType.COS_NAME) {
                        descriptorCharSet.add(glyphName.getString());
                    }
                    glyphName = parser.nextObject();
                }
                return descriptorCharSet;
            } catch (IOException ex) {
                LOGGER.log(Level.FINE, "Can't parse /CharSet entry in Type 1 font descriptor", ex);
                return Collections.emptySet();
            }
        }
        return Collections.emptySet();
    }

    @Override
    public FontProgram getFontProgram() {
        if (this.isFontParsed) {
            return this.fontProgram;
        }
        this.isFontParsed = true;
        if (fontDescriptor.canParseFontFile(ASAtom.FONT_FILE)) {
            COSStream type1FontFile = fontDescriptor.getFontFile();
            try (ASInputStream fontData = type1FontFile.getData(COSStream.FilterFlags.DECODE)) {
                this.fontProgram = new Type1FontProgram(
                        fontData, this.getEncodingMapping());
                return this.fontProgram;
            } catch (IOException e) {
                LOGGER.log(Level.FINE, "Can't read Type 1 font program.", e);
            }
        } else if (fontDescriptor.canParseFontFile(ASAtom.FONT_FILE3)) {
            COSStream type1FontFile = fontDescriptor.getFontFile3();
            ASAtom subtype = type1FontFile.getNameKey(ASAtom.SUBTYPE);
            try (ASInputStream fontData = type1FontFile.getData(COSStream.FilterFlags.DECODE)) {
                if (subtype == ASAtom.TYPE1C) {

                    this.fontProgram = new CFFFontProgram(fontData, this.getEncodingMapping(),
                            null, this.isSubset());
                    return this.fontProgram;
                } else if (subtype == ASAtom.OPEN_TYPE) {
                    this.fontProgram = new OpenTypeFontProgram(fontData, true, this.isSymbolic(),
                            this.getEncoding(), null, this.isSubset());
                    return this.fontProgram;
                }
            } catch (IOException e) {
                LOGGER.log(Level.FINE, "Can't read Type 1 font program.", e);
            }
        }
        this.fontProgram = null;
        return null;
    }

    public Boolean isStandard() {
        if (this.isStandard == null) {
            if (!containsDiffs() && !isEmbedded() && isNameStandard()) {
                isStandard = Boolean.valueOf(true);
                return isStandard;
            }
            isStandard = Boolean.valueOf(false);
            return isStandard;
        }
        return this.isStandard;
    }

    private boolean containsDiffs() {
        if (this.dictionary.getKey(ASAtom.ENCODING).getType() ==
                COSObjType.COS_DICT) {
            Map<Integer, String> differences = this.getDifferences();
            if (differences != null && differences.size() != 0) {
                String[] baseEncoding = getBaseEncoding((COSDictionary)
                        this.dictionary.getKey(ASAtom.ENCODING).getDirectBase());
                if (baseEncoding.length == 0) {
                    return true;
                }
                for (Map.Entry<Integer, String> entry : differences.entrySet()) {
                    if (!entry.getValue().equals(baseEncoding[entry.getKey().intValue()])) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private static String[] getBaseEncoding(COSDictionary encoding) {
        ASAtom baseEncoding = encoding.getNameKey(ASAtom.BASE_ENCODING);
        if (baseEncoding == null) {
            return new String[]{};
        }
        if (baseEncoding == ASAtom.MAC_ROMAN_ENCODING) {
            return Arrays.copyOf(TrueTypePredefined.MAC_ROMAN_ENCODING,
                    TrueTypePredefined.MAC_ROMAN_ENCODING.length);
        } else if (baseEncoding == ASAtom.MAC_EXPERT_ENCODING) {
            return Arrays.copyOf(TrueTypePredefined.MAC_EXPERT_ENCODING,
                    TrueTypePredefined.MAC_EXPERT_ENCODING.length);
        } else if (baseEncoding == ASAtom.WIN_ANSI_ENCODING) {
            return Arrays.copyOf(TrueTypePredefined.WIN_ANSI_ENCODING,
                    TrueTypePredefined.WIN_ANSI_ENCODING.length);
        } else {
            return new String[]{};
        }
    }

    private boolean isEmbedded() {
        return this.getFontProgram() != null;
    }

    private boolean isNameStandard() {
        ASAtom fontName = this.getDictionary().getNameKey(ASAtom.BASE_FONT);
        for (ASAtom standard : STANDARD_FONT_NAMES) {
            if (standard == fontName) {
                return true;
            }
        }
        return false;
    }
}
