/**
 * This file is part of veraPDF Parser, a module of the veraPDF project.
 * Copyright (c) 2015-2025, veraPDF Consortium <info@verapdf.org>
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
package org.verapdf.pd.function;

import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.params.converter.ArgumentConversionException;
import org.junit.jupiter.params.converter.ArgumentConverter;
import org.verapdf.cos.*;
import org.verapdf.parser.FunctionParser;
import org.verapdf.parser.postscript.PSOperator;

import java.util.ArrayList;
import java.util.List;

class PDFunctionTestHelper {
    public static final double EPSILON = 1.0E-7;

    public static class ListOfCOSObjectsConverter implements ArgumentConverter {
        @Override
        public List<COSObject> convert(Object source, ParameterContext parameterContext) throws ArgumentConversionException {
            List<COSObject> params = new ArrayList<>();
            String[] parts = ((String) source).split(" ");
            for (String item : parts) {
                if (FunctionParser.FUNCTION_KEYWORDS.contains(item)) {
                    params.add(new PSOperator(COSName.construct(item)));
                } else {
                    params.add(COSReal.construct(Double.parseDouble(item)));
                }
            }
            return params;
        }
    }
}
