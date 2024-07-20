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

import java.util.List;
import java.util.stream.Stream;

import static org.verapdf.pd.function.PDFunctionTestHelper.EPSILON;

class PDType2FunctionTest {
    final PDType2Function func = new PDType2Function(new COSObject());

    @ParameterizedTest
    @MethodSource("provideParameters")
    public void testGetResult(double N,
                              @ConvertWith(PDFunctionTestHelper.ListOfCOSObjectsConverter.class) List<COSObject> operands,
                              @ConvertWith(PDFunctionTestHelper.ListOfCOSObjectsConverter.class) List<COSObject> result) {
        func.setN(N);
        List<COSObject> actualResult = func.getResult(operands);
        Assertions.assertEquals(result.size(), actualResult.size());
        for (int i = 0; i < result.size(); ++i) {
            Assertions.assertEquals(result.get(i).getReal(), actualResult.get(i).getReal(), EPSILON);
        }
    }

    private static Stream<Arguments> provideParameters() {
        return Stream.of(
                Arguments.of(2, "3", "9"),
                Arguments.of(-2, "4", "0.0625"),
                Arguments.of(0.5, "25", "5")
        );
    }

    @ParameterizedTest
    @MethodSource("provideInvalidParameters")
    public void testGetResultIsNull(double N,
                                    @ConvertWith(PDFunctionTestHelper.ListOfCOSObjectsConverter.class) List<COSObject> operands) {
        func.setN(N);
        Assertions.assertNull(func.getResult(operands));
    }

    private static Stream<Arguments> provideInvalidParameters() {
        return Stream.of(
                Arguments.of(-2, "0"),
                Arguments.of(0.5, "-4"),
                Arguments.of(-0.5, "-4"),
                Arguments.of(-0.5, "0")
        );
    }

    @ParameterizedTest
    @MethodSource("provideParametersWithNOutputValues")
    public void testGetResultWithNOutput(double N,
                                         @ConvertWith(PDFunctionTestHelper.ListOfCOSObjectsConverter.class) List<COSObject> C0,
                                         @ConvertWith(PDFunctionTestHelper.ListOfCOSObjectsConverter.class) List<COSObject> C1,
                                         @ConvertWith(PDFunctionTestHelper.ListOfCOSObjectsConverter.class) List<COSObject> operands,
                                         @ConvertWith(PDFunctionTestHelper.ListOfCOSObjectsConverter.class) List<COSObject> result) {
        func.setN(N);
        func.setC0(C0);
        func.setC1(C1);
        List<COSObject> actualResult = func.getResult(operands);
        Assertions.assertEquals(result.size(), actualResult.size());
        for (int i = 0; i < result.size(); ++i) {
            Assertions.assertEquals(result.get(i).getReal(), actualResult.get(i).getReal(), EPSILON);
        }
    }

    private static Stream<Arguments> provideParametersWithNOutputValues() {
        return Stream.of(
                Arguments.of(2, "1.0 2.0 3.0", "2.0 4.0 6.0", "2", "5 10 15")
        );
    }
}
