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
package org.verapdf.pd;

import org.verapdf.as.ASAtom;
import org.verapdf.cos.*;
import org.verapdf.factory.fonts.PDFontFactory;
import org.verapdf.pd.font.PDFont;
import org.verapdf.pd.function.PDFunction;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

/**
 * @author Maksim Bezrukov
 */
public class PDExtGState extends PDResource {

    private static final Logger LOGGER = Logger.getLogger(PDExtGState.class.getCanonicalName());

    public PDExtGState(COSObject obj) {
        super(obj);
    }

    public Boolean getAlphaSourceFlag() {
        return getBooleanKey(ASAtom.AIS);
    }

    public Boolean getAutomaticStrokeAdjustment() {
        return getBooleanKey(ASAtom.SA);
    }

    public Boolean getStrokingOverprintControl() {
        return getBooleanKey(ASAtom.OP);
    }

    public Boolean getNonStrokingOverprintControl() {
        Boolean opNS = getBooleanKey(ASAtom.OP_NS);
        return opNS == null ? getBooleanKey(ASAtom.OP) : opNS;
    }

    public Long getOverprintMode() {
         return getIntegerKey(ASAtom.OPM);
    }

    public COSObject getCOSTR() {
        return getKey(ASAtom.TR);
    }

    public COSObject getCOSTR2() {
        return getKey(ASAtom.TR2);
    }

    public COSObject getCOSSMask() {
        return getKey(ASAtom.SMASK);
    }

    public COSObject getCOSBM() {
        return getKey(ASAtom.BM);
    }

    public COSObject getCA() {
        return getKey(ASAtom.CA);
    }

    public COSObject getCA_NS() {
        return getKey(ASAtom.CA_NS);
    }

    public COSName getCOSRenderingIntentName() {
        COSObject name = getKey(ASAtom.RI);
        if (name != null && name.getType() == COSObjType.COS_NAME) {
            return (COSName) name.getDirectBase();
        }
        return null;
    }

    public COSNumber getCOSFontSize() {
        COSObject fontArray = getKey(ASAtom.FONT);
        if (fontArray != null && fontArray.getType() == COSObjType.COS_ARRAY) {
            COSObject res = fontArray.at(1);
            if (res != null && res.getType().isNumber()) {
                return (COSNumber) res.getDirectBase();
            }
        }
        return null;
    }

    public PDHalftone getHalftone() {
        COSObject obj = getKey(ASAtom.HT);
        if (obj != null &&
                (obj.getType() == COSObjType.COS_NAME
                        || obj.getType() == COSObjType.COS_DICT
                        || obj.getType() == COSObjType.COS_STREAM)) {
            return new PDHalftone(obj);
        }
        return null;
    }

    public COSObject getHalftonePhase() {
        return getKey(ASAtom.HTP);
    }

    public PDFont getFont() {
        COSObject fontArray = getKey(ASAtom.FONT);
        if (fontArray != null && fontArray.getType() == COSObjType.COS_ARRAY) {
            COSObject res = fontArray.at(0);
            if (res != null && res.getType().isDictionaryBased()) {
                return PDFontFactory.getPDFont(res);
            }
        }
        return null;
    }

    public List<PDFunction> getTRFunctions() {
        return getFunctions(ASAtom.TR);
    }

    public List<PDFunction> getTR2Functions() {
        return getFunctions(ASAtom.TR2);
    }

    private List<PDFunction> getFunctions(ASAtom atom) {
        COSObject functions = getKey(atom);
        if (functions == null) {
            return Collections.emptyList();
        }
        if (functions.getType() == COSObjType.COS_STREAM) {
            PDFunction function = PDFunction.createFunction(functions);
            if (function != null) {
                return Collections.singletonList(function);
            }
        } else if (functions.getType() == COSObjType.COS_ARRAY) {
            if (functions.size() != 4) {
                LOGGER.warning("Transfer function array must contain exactly 4 elements");
            }
            List<PDFunction> resultList = new ArrayList<>();
            for (int i = 0; i < functions.size(); i++) {
                PDFunction function = PDFunction.createFunction(functions.at(i));
                if (function != null) {
                    resultList.add(function);
                }
            }
            return Collections.unmodifiableList(resultList);
        }
        return Collections.emptyList();
    }
}
