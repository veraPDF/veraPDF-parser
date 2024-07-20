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
package org.verapdf.pd.function;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.converter.ConvertWith;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.verapdf.cos.COSObject;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.verapdf.pd.function.PDFunctionTestHelper.EPSILON;

class PDType3FunctionTest {
    final PDType3Function func = new PDType3Function(new COSObject());
    final PDType2Function func2 = new PDType2Function(new COSObject());
    final PDType4Function func4 = new PDType4Function(new COSObject());

    @ParameterizedTest
    @MethodSource("provideParameters")
    public void testGetResult(@ConvertWith(PDFunctionTestHelper.ListOfCOSObjectsConverter.class) List<COSObject> operators,
                              @ConvertWith(PDFunctionTestHelper.ListOfCOSObjectsConverter.class) List<COSObject> operands,
                              @ConvertWith(PDFunctionTestHelper.ListOfCOSObjectsConverter.class) List<COSObject> subdomain,
                              double N,
                              @ConvertWith(PDFunctionTestHelper.ListOfCOSObjectsConverter.class) List<COSObject> result) {
        func4.setOperators(operators);
        func2.setN(N);
        func.setSubdomains(subdomain);
        List<PDFunction> funcs = new ArrayList<>();
        funcs.add(func2);
        funcs.add(func4);
        func.setFunctions(funcs);
        List<COSObject> actualResult = func.getResult(operands);
        Assertions.assertEquals(result.size(), actualResult.size());
        for (int i = 0; i < result.size(); ++i) {
            Assertions.assertEquals(result.get(i).getReal(), actualResult.get(i).getReal(), EPSILON);
        }
    }


    private static Stream<Arguments> provideParameters() {
        return Stream.of(
                Arguments.of("{ dup sub 0 eq { 125 5 div sqrt } if }", "5", "0.0 4.0 10.0", 2, "5"),
                Arguments.of("{ dup sub 0 eq { 125 5 div sqrt } if }", "3", "0.0 4.0 10.0", 2, "9"),
                Arguments.of("{ dup sub 0 eq { 125 5 div sqrt } if }", "-3", "0.0 4.0 10.0", 2, "0"),
                Arguments.of("{ dup sub 0 eq { 125 5 div sqrt } if }", "15", "0.0 4.0 10.0", 2, "5")
        );
    }
}
