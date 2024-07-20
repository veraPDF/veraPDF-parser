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

import org.verapdf.as.ASAtom;
import org.verapdf.as.io.ASInputStream;
import org.verapdf.cos.*;

import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class PDType0Function extends PDFunction {
    private BitSet sampleTable;
    private final COSArray size;
    private final COSArray domain;
    private final COSArray encode;
    private final COSArray decode;
    private final int outputDimension = getRange().size() / 2;
    private List<Integer> sizesProducts;
    private long numberOfSampleBytes = 0;

    private static final Logger LOGGER = Logger.getLogger(PDType0Function.class.getCanonicalName());
    private static final long ALLOWABLE_MEMORY_CAPACITY = 100000000;

    protected PDType0Function(COSObject obj) {
        super(obj);
        size = getSize();
        domain = getDomain();
        encode = getEncode();
        decode = getDecode();
        sizesProducts = getSizesProducts();
    }

    public COSArray getEncode() {
        COSArray encode = getCOSArray(ASAtom.ENCODE);
        if (encode == null) {
            encode = getDefaultEncode();
        }
        return encode;
    }

    private COSArray getDefaultEncode() {
        List<COSObject> encodeFromSize = new ArrayList<>();
        for (int i = 0; i < size.size(); ++i) {
            encodeFromSize.add(COSReal.construct(0));
            encodeFromSize.add(COSReal.construct(size.at(i).getInteger() - 1));
        }
        return new COSArray(encodeFromSize);
    }

    public COSArray getDecode() {
        COSArray decode = getCOSArray(ASAtom.DECODE);
        if (decode == null) {
            decode = getRange();
        }
        return decode;
    }

    public Long getBitsPerSample() {
        Long bitsPerSample = getIntegerKey(ASAtom.BITS_PER_SAMPLE);
        List<Long> validBitsPerSample = Arrays.asList(1L, 2L, 4L, 8L, 12L, 16L, 24L, 32L);
        if (validBitsPerSample.contains(bitsPerSample)) {
            return bitsPerSample;
        } else {
            LOGGER.log(Level.WARNING, "Invalid BitsPerSample key value in Type 0 Functions");
        }
        return null;
    }

    public Long getOrder() {
        Long order = getIntegerKey(ASAtom.ORDER);
        if (order == null || (order != 1L && order != 3L)) {
            return 1L;
        }
        for (COSObject value : size) {
            if (value.getInteger() < 4) {
                return 1L;
            }
        }
        return order;
    }

    private COSArray getSize() {
        return getCOSArray(ASAtom.SIZE);
    }

    private List<Integer> getSizesProducts() {
        if (sizesProducts == null) {
            sizesProducts = new ArrayList<>();
            int valueToBeAdded = 1;
            sizesProducts.add(valueToBeAdded);
            for (COSObject item : size) {
                valueToBeAdded *= item.getInteger().intValue();
                sizesProducts.add(valueToBeAdded);
            }
        }
        return sizesProducts;
    }

    public BitSet getSampleTable() {
        if (sampleTable == null) {
            sampleTable = getSamples();
        }
        return sampleTable;
    }

    private long getNumberOfSampleBytes() {
        if (numberOfSampleBytes == 0) {
            double num = 1.0;
            for (COSObject item : size) {
                num *= item.getInteger();
            }
            numberOfSampleBytes = (long) Math.ceil(num * getBitsPerSample() / 8.0 * outputDimension);
        }
        return numberOfSampleBytes;
    }

    private BitSet getSamples() {
        COSObject obj = this.getObject();
        if (obj.getType() != COSObjType.COS_STREAM) {
            LOGGER.log(Level.WARNING, "Invalid stream for type 0 function");
            return new BitSet();
        }
        try (ASInputStream functionStream = getObject().getData(COSStream.FilterFlags.DECODE)) {
            byte[] bytes = new byte[(int) getNumberOfSampleBytes()];
            functionStream.read(bytes);
            return BitSet.valueOf(bytes);
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Can not parse function", e);
            return new BitSet();
        }
    }

    private int bitSetToUnsignedInt(BitSet b, int startBit, int length) {
        int value = 0;
        int bitValue = 1;
        for (int i = 0; i < length; i++) {
            if (b.get(startBit + i)) {
                value += bitValue;
            }
            bitValue += bitValue;
        }
        return value;
    }

    private List<Integer> getSampleValue(List<Integer> coordinates) {
        int startIndex = 0;
        for (int i = coordinates.size() - 1; i >= 0; --i) {
            startIndex += coordinates.get(i) * sizesProducts.get(i) * getBitsPerSample().intValue() * outputDimension;
        }
        int endIndex = startIndex + getBitsPerSample().intValue() * outputDimension;
        List<Integer> resultValues = new ArrayList<>();
        while (startIndex < endIndex) {
            resultValues.add(bitSetToUnsignedInt(getSampleTable(), startIndex, getBitsPerSample().intValue()));
            startIndex += getBitsPerSample().intValue();
        }
        return resultValues;
    }

    @Override
    public List<COSObject> getResult(List<COSObject> ops) {
        try {
            if (getNumberOfSampleBytes() > ALLOWABLE_MEMORY_CAPACITY) {
                LOGGER.log(Level.WARNING, "Type 0 function sample stream requires more than " +
                        (int) (ALLOWABLE_MEMORY_CAPACITY / 1E6) + "Mb of data. " +
                        "The result of the function will not be evaluated. ");
                return null;
            }
            List<COSObject> operands = getValuesInIntervals(ops, getDomain());
            for (int i = 0; i < operands.size(); ++i) {
                operands.set(i, interpolate(operands.get(i), domain.at(2 * i), domain.at(2 * i + 1), encode.at(2 * i), encode.at(2 * i + 1)));
            }
            operands = getValuesInIntervals(operands, getDefaultEncode());
            List<Double> doubleOperands = operands.stream().map(COSObject::getReal).collect(Collectors.toList());
            List<COSObject> interpolationResult = getOrder().intValue() == 1 ?
                    multiLinearInterpolation(doubleOperands) :
                    multiCubicInterpolation(doubleOperands);
            List<COSObject> result = new ArrayList<>();
            for (int i = 0; i < outputDimension; ++i) {
                result.add(interpolate(interpolationResult.get(i), COSReal.construct(0),
                        COSReal.construct(Math.pow(2, getBitsPerSample()) - 1), decode.at(2 * i), decode.at(2 * i + 1)));
            }
            return Collections.unmodifiableList(getValuesInIntervals(result, getRange()));
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error while evaluating type 0 function", e);
            return null;
        }
    }

    private List<BitSet> getLinearNCombinations(int n) {
        List<BitSet> result = new ArrayList<>();
        long N = 1L << n;
        for (long bits = 0; bits < N; ++bits) {
            BitSet bitSet = new BitSet(n);
            bitSet.or(BitSet.valueOf(new long[]{bits}));
            result.add(bitSet);
        }
        return result;
    }

    private List<List<Integer>> getCubicNCombinations(int n) {
        List<List<Integer>> result = new ArrayList<>();
        int N = 1 << (2 * n);
        for (int bits = N - 1; bits >= 0; --bits) {
            List<Integer> combination = new ArrayList<>();
            int currentValue = bits;
            for (int i = n - 1; i >= 0; --i) {
                int val = (int) Math.pow(4, i);
                if (val > currentValue) {
                    combination.add(-1);
                } else if (val * 3 <= currentValue) {
                    combination.add(2);
                    currentValue -= val * 3;
                } else if (val * 2 <= currentValue) {
                    combination.add(1);
                    currentValue -= val * 2;
                } else {
                    combination.add(0);
                    currentValue -= val;
                }
            }
            result.add(combination);
        }
        return result;
    }

    private List<COSObject> multiLinearInterpolation(List<Double> x) {
        try {
            List<Integer> leftX = new ArrayList<>();
            List<Integer> rightX = new ArrayList<>();
            List<Double> alpha = new ArrayList<>();
            for (int i = 0; i < x.size(); ++i) {
                if (size.at(i).getInteger() == 1) {
                    leftX.add(0);
                    rightX.add(0);
                    alpha.add(1.0);
                } else {
                    leftX.add((int) Math.floor(x.get(i)));
                    rightX.add(leftX.get(i) + 1);
                    alpha.add(x.get(i) - leftX.get(i));
                }
            }
            List<List<Double>> multipliedByCoefficientsSampleValues = new ArrayList<>();
            for (BitSet combination : getLinearNCombinations(x.size())) {
                List<Integer> sampleX = new ArrayList<>();
                Double coefficientForSampleValue = 1.0;
                for (int i = 0; i < x.size(); ++i) {
                    if (combination.get(i)) {
                        sampleX.add(leftX.get(i));
                        coefficientForSampleValue *= 1 - alpha.get(i);
                    } else {
                        sampleX.add(rightX.get(i));
                        coefficientForSampleValue *= alpha.get(i);
                    }
                }
                Double finalFactor = coefficientForSampleValue;
                multipliedByCoefficientsSampleValues.add(getSampleValue(sampleX).stream().map(o -> o * finalFactor).collect(Collectors.toList()));
            }
            List<COSObject> result = new ArrayList<>();
            for (int i = 0; i < outputDimension; ++i) {
                Double res = 0.0;
                for (List<Double> value : multipliedByCoefficientsSampleValues) {
                    res += value.get(i);
                }
                result.add(COSReal.construct(res));
            }
            return result;
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Failed to get interpolant coefficients", e);
            return null;
        }
    }

    private List<COSObject> multiCubicInterpolation(List<Double> x) {
        try {
            List<List<Double>> samples = new ArrayList<>();
            for (List<Integer> combination : getCubicNCombinations(x.size())) {
                List<Integer> sampleX = new ArrayList<>();
                for (int i = 0; i < x.size(); ++i) {
                    sampleX.add((int) Math.max(0, Math.min(size.at(i).getInteger() - 1, Math.floor(x.get(i)) + combination.get(i))));
                }
                samples.add(getSampleValue(sampleX).stream().map(v -> (double) v).collect(Collectors.toList()));
            }
            List<Double> interpolateResult = nCubicInterpolate(x.size(), samples, x);
            List<COSObject> result = new ArrayList<>();
            for (Double value : interpolateResult) {
                result.add(COSReal.construct(value));
            }
            return result;
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Failed to get interpolant coefficients", e);
            return null;
        }
    }
    
    private List<Double> nCubicInterpolate(int n, List<List<Double>> p, List<Double> coordinates) {
        if (n == 1) {
            return cubicInterpolate(p, coordinates.get(0) - Math.floor(coordinates.get(0)));
        } else {
            List<List<Double>> arr = new ArrayList<>();
            int skip = 1 << (n - 1) * 2;
            arr.add(nCubicInterpolate(n - 1, p, coordinates.subList(1, coordinates.size())));
            arr.add(nCubicInterpolate(n - 1, p.subList(skip, p.size()), coordinates.subList(1, coordinates.size())));
            arr.add(nCubicInterpolate(n - 1, p.subList(2 * skip, p.size()), coordinates.subList(1, coordinates.size())));
            arr.add(nCubicInterpolate(n - 1, p.subList(3 * skip, p.size()), coordinates.subList(1, coordinates.size())));
            return cubicInterpolate(arr, coordinates.get(0) - Math.floor(coordinates.get(0)));
        }
    }

    private List<Double> cubicInterpolate(List<List<Double>> adjacentSampleValues, double x) {
        List<Double> result = new ArrayList<>();
        for (int i = 0; i < outputDimension; ++i) {
            result.add(adjacentSampleValues.get(2).get(i) + 0.5 * x * (adjacentSampleValues.get(1).get(i) -
                    adjacentSampleValues.get(3).get(i) + x * (2.0 * adjacentSampleValues.get(3).get(i) -
                    5.0 * adjacentSampleValues.get(2).get(i) + 4.0 * adjacentSampleValues.get(1).get(i) -
                    adjacentSampleValues.get(0).get(i) + x * (3.0 * (adjacentSampleValues.get(2).get(i) -
                    adjacentSampleValues.get(1).get(i)) + adjacentSampleValues.get(0).get(i) - adjacentSampleValues.get(3).get(i)))));
        }
        return result;
    }
}
