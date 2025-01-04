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

import org.verapdf.as.ASAtom;
import org.verapdf.cos.COSArray;
import org.verapdf.cos.COSObject;
import org.verapdf.cos.COSReal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PDType2Function extends PDFunction {
    private static final Double ZERO = 0.0;
    private static final Double ONE = 1.0;
    private static final double EPS = 1.0E-7;
    private List<COSObject> C0;
    private List<COSObject> C1;
    private Double N;
    private static final Logger LOGGER = Logger.getLogger(PDType2Function.class.getCanonicalName());

    protected PDType2Function(COSObject obj) {
        super(obj);
        C0 = getCOSArrayAsList(ASAtom.C0, ZERO);
        C1 = getCOSArrayAsList(ASAtom.C1, ONE);
    }

    private List<COSObject> getCOSArrayAsList(final ASAtom key, double defaultValue) {
        List<COSObject> result = new ArrayList<>();
        COSArray array = getCOSArray(key);
        if (array == null) {
            result.add(COSReal.construct(defaultValue));
        } else {
            for (COSObject item : array) {
                result.add(item);
            }
        }
        return result;
    }

    public Double getN() {
        if (N == null) {
            if (getObject().getKey(ASAtom.N) == null) {
                LOGGER.log(Level.WARNING, "Invalid N key value in Type 2 Function dictionary");
                return null;
            }
            N = getObject().getKey(ASAtom.N).getReal();
        }
        return N;
    }

    public void setN(double N) {
        this.N = N;
    }

    public void setC0(List<COSObject> C0) {
        this.C0 = C0;
    }

    public void setC1(List<COSObject> C1) {
        this.C1 = C1;
    }

    private boolean checkOperand(COSObject operand) {
        if (Math.abs(N - Math.floor(N)) > EPS) {
            if (N < 0) {
                return operand.getReal() > ZERO;
            }
            return operand.getReal() >= ZERO;
        }
        if (N < 0) {
            return !ZERO.equals(operand.getReal());
        }
        return true;
    }

    @Override
    public List<COSObject> getResult(List<COSObject> ops) {
        if (ops.size() > 1) {
            LOGGER.log(Level.WARNING, "Too many operands. The first one will be chosen");
        }
        List<COSObject> operands = new ArrayList<>();
        operands.add(ops.get(0));
        operands = getValuesInIntervals(operands, getDomain());
        COSObject operand = operands.get(0);
        if (getN() == null || !checkOperand(operand)) {
            LOGGER.log(Level.WARNING, "Invalid operands stream or N key value in Type 2 Function dictionary");
            return null;
        }
        if (ZERO.equals(operand.getReal())) {
            return C0;
        }
        if (ONE.equals(operand.getReal())) {
            return C1;
        }
        List<COSObject> result = new ArrayList<>();
        for (int i = 0; i < C0.size(); ++i) {
            result.add(COSReal.construct(C0.get(i).getReal()
                    + (C1.get(i).getReal() - C0.get(i).getReal()) *
                    Math.pow(operand.getReal(), N)));
        }
        return Collections.unmodifiableList(getValuesInIntervals(result, getRange()));
    }
}
