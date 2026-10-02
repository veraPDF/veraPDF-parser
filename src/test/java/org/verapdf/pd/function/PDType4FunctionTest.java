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
package org.verapdf.pd.function;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.converter.ConvertWith;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.verapdf.cos.COSName;
import org.verapdf.cos.COSObject;
import org.verapdf.parser.postscript.PSOperator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static org.verapdf.pd.function.PDFunctionTestHelper.EPSILON;
import static org.verapdf.pd.function.PSOperatorsConstants.LEFT_CURLY_BRACE;
import static org.verapdf.pd.function.PSOperatorsConstants.RIGHT_CURLY_BRACE;

public class PDType4FunctionTest {
    final PDType4Function func = new PDType4Function(new COSObject());

    @ParameterizedTest
    @MethodSource("provideParameters")
    public void testGetResult(@ConvertWith(PDFunctionTestHelper.ListOfCOSObjectsConverter.class) List<COSObject> operators,
                              @ConvertWith(PDFunctionTestHelper.ListOfCOSObjectsConverter.class) List<COSObject> operands,
                              @ConvertWith(PDFunctionTestHelper.ListOfCOSObjectsConverter.class) List<COSObject> result) {
        func.setOperators(operators);
        List<COSObject> actualResult = func.getResult(operands);
        Assertions.assertEquals(result.size(), actualResult.size());
        for (int i = 0; i < result.size(); ++i) {
            Assertions.assertEquals(result.get(i).getReal(), actualResult.get(i).getReal(), EPSILON);
        }
    }

    private static Stream<Arguments> provideParameters() {
        return Stream.of(
                Arguments.of("{ 2 mul neg 2 div exch 2 mul sqrt 2 div add }", "2 3", "-2"),
                Arguments.of("{ dup add 9 le { dup mul dup add } { 1 sub dup mul } ifelse }", "2 3", "8"),
                Arguments.of("{ dup add 9 le { dup mul dup add } { 1 sub dup mul } ifelse }", "5 6", "16"),
                Arguments.of("{ dup sub 0 eq { 125 5 div sqrt } if }", "5", "5"),
                Arguments.of("{ 3 3 1 roll }", "1 2", "3 1 2"),
                Arguments.of("{ 3 2 roll }", "1 2 3 4", "1 3 4 2"),
                Arguments.of("{ 3 -2 roll }", "1 2 3 4", "1 4 2 3")
        );
    }

    @ParameterizedTest
    @MethodSource("provideInvalidParameters")
    public void testGetResultWithInvalidOperators(@ConvertWith(PDFunctionTestHelper.ListOfCOSObjectsConverter.class) List<COSObject> operators,
                              @ConvertWith(PDFunctionTestHelper.ListOfCOSObjectsConverter.class) List<COSObject> operands) {
        func.setOperators(operators);
        Assertions.assertNull(func.getResult(operands));
    }

    @Test
    public void testGetResultWithExcessiveProcedureNesting() {
        List<COSObject> operators = new ArrayList<>();
        for (int i = 0; i < 8001; i++) {
            operators.add(new PSOperator(COSName.construct(LEFT_CURLY_BRACE)));
        }
        for (int i = 0; i < 8001; i++) {
            operators.add(new PSOperator(COSName.construct(RIGHT_CURLY_BRACE)));
        }

        func.setOperators(operators);
        Assertions.assertNull(func.getResult(Collections.emptyList()));
        Assertions.assertNull(func.getResult(Collections.emptyList()));
    }

    private static Stream<Arguments> provideInvalidParameters() {
        return Stream.of(
                Arguments.of("{ add }", "2")
        );
    }
}
