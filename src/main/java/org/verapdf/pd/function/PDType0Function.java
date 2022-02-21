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

    private static final Logger LOGGER = Logger.getLogger(PDType2Function.class.getCanonicalName());

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

    private int getNumberOfSampleBytes() {
        double num = 1.0;
        for (COSObject item : size) {
            num *= item.getInteger();
        }
        return (int) Math.ceil(num * getBitsPerSample() / 8.0 * outputDimension);
    }

    private BitSet getSamples() {
        COSObject obj = this.getObject();
        if (obj.getType() != COSObjType.COS_STREAM) {
            LOGGER.log(Level.WARNING, "Invalid stream for type 0 function");
            return new BitSet();
        }
        try (ASInputStream functionStream = getObject().getData(COSStream.FilterFlags.DECODE)) {
            byte[] bytes = new byte[getNumberOfSampleBytes()];
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
            List<COSObject> operands = getValuesInIntervals(ops, getDomain());
            List<COSObject> result = new ArrayList<>();
            for (int i = 0; i < operands.size(); ++i) {
                operands.set(i, interpolate(operands.get(i), domain.at(2 * i), domain.at(2 * i + 1), encode.at(2 * i), encode.at(2 * i + 1)));
            }
            operands = getValuesInIntervals(operands, getDefaultEncode());
            List<Double> doubleOperands = operands.stream().map(COSObject::getReal).collect(Collectors.toList());
            List<COSObject> interpolationResult = getOrder().intValue() == 1 ?
                    multiLinearInterpolation(doubleOperands) :
                    multiCubicInterpolation(doubleOperands);
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
        return multiLinearInterpolation(x);
    }
}
