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
package org.verapdf.pd.font.type3;

import org.verapdf.as.ASAtom;
import org.verapdf.cos.COSDictionary;
import org.verapdf.cos.COSObjType;
import org.verapdf.cos.COSObject;
import org.verapdf.cos.COSStream;
import org.verapdf.pd.PDResources;
import org.verapdf.pd.font.FontProgram;
import org.verapdf.pd.font.PDSimpleFont;
import org.verapdf.tools.TypeConverter;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Sergey Shemyakov
 */
public class PDType3Font extends PDSimpleFont {

    private static final Logger LOGGER = Logger.getLogger(PDType3Font.class.getCanonicalName());

    public PDType3Font(COSDictionary dictionary) {
        super(dictionary);
        this.setSuccessfullyParsed(true);
    }

    /**
     * @return dictionary with char proc values.
     */
    public COSDictionary getCharProcDict() {
        return (COSDictionary) this.dictionary.getKey(ASAtom.CHAR_PROCS).getDirectBase();
    }

    @Override
    public FontProgram getFontProgram() {
        return null;
    }

    /**
     * @return resources as presented in type 3 font dictionary.
     */
    public PDResources getResources() {
        COSObject resources = this.dictionary.getKey(ASAtom.RESOURCES);
        if (!resources.empty() && resources.getType() == COSObjType.COS_DICT) {
            if (resources.isIndirect()) {
                resources = resources.getDirect();
            }
            return new PDResources(resources);
        } else {
            return new PDResources(COSDictionary.construct());
        }
    }

    @Override
    public String getName() {
        return this.dictionary.getStringKey(ASAtom.NAME);
    }

    /**
     * Checks if char proc dictionary contains char proc for glyph with given
     * code.
     *
     * @param code is the code of glyph.
     * @return true if char proc for this glyph is present.
     */
    public boolean containsCharString(int code) {
        return !getCharProc(code).empty();
    }

    /**
     * @return a rectangle, expressed in the glyph coordinate system, that shall
     * specify the font bounding box.
     */
    public double[] getFontBoundingBox() {
        COSObject bbox = getKey(ASAtom.FONT_BBOX);
        if (bbox.getType() == COSObjType.COS_ARRAY || bbox.size() == 4) {
            double[] res = new double[4];
            for (int i = 0; i < 4; ++i) {
                COSObject obj = bbox.at(i);
                if (obj.getType().isNumber()) {
                    res[i] = obj.getReal();
                } else {
                    String fontName = getName() == null ? "" : getName();
                    LOGGER.log(Level.FINE, "Font bounding box array for font " + fontName +
                            " contains " + obj.getType());
                    return null;
                }
            }
            return res;
        } else {
            String fontName = getName() == null ? "" : getName();
            LOGGER.log(Level.FINE, "Font bounding box array for font " + fontName +
                    " is not an array of 4 elements");
            return null;
        }
    }

    public double[] getFontMatrix() {
        return TypeConverter.getRealArray(getKey(ASAtom.FONT_MATRIX), 6, "Font matrix");
    }

    private COSDictionary getCharProcs() {
        return (COSDictionary) this.dictionary.getKey(ASAtom.CHAR_PROCS).getDirectBase();
    }

    private COSObject getCharProc(int code) {
        String glyphName = this.getEncodingMapping().getName(code);
        COSDictionary charProcs = this.getCharProcs();
        if (charProcs != null) {
            ASAtom asAtomGlyph = ASAtom.getASAtom(glyphName);
            return charProcs.getKey(asAtomGlyph);
        }
        return COSObject.getEmpty();
    }

    @Override
    public float getWidthFromProgram(int code) {
        COSObject charProc = getCharProc(code);
        if (charProc.getType() == COSObjType.COS_STREAM) {
            try (Type3CharProcParser parser = new Type3CharProcParser(charProc.getData(COSStream.FilterFlags.DECODE))) {
                parser.parse();
                return (float) parser.getWidth();
            } catch (IOException e) {
                LOGGER.log(Level.FINE, "Can't get width from type 3 char proc", e);
            }
        }
        return -1;
    }

    @Override
    public boolean glyphIsPresent(int code) {
        return containsCharString(code);
    }
}
