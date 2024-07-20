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
import org.verapdf.cos.COSArray;
import org.verapdf.cos.COSObjType;
import org.verapdf.cos.COSObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PDType3Function extends PDFunction {
    private COSArray domain;
    private List<COSObject> subdomains;
    private List<PDFunction> functions;
    private static final Logger LOGGER = Logger.getLogger(PDType3Function.class.getCanonicalName());

    protected PDType3Function(COSObject obj) {
        super(obj);
        domain = getDomain();
        subdomains = getSubdomains();
        functions = getFunctions();
    }

    public void setSubdomains(List<COSObject> subdomains) {
        this.subdomains = subdomains;
        List<COSObject> dom = new ArrayList<>();
        dom.add(subdomains.get(0));
        dom.add(subdomains.get(subdomains.size() - 1));
        this.domain = new COSArray(dom);
    }

    public void setFunctions(List<PDFunction> functions) {
        this.functions = functions;
    }

    public List<COSObject> getSubdomains() {
        if (subdomains == null) {
            if (domain == null) {
                LOGGER.log(Level.WARNING, "Invalid Domain key value in Type 3 Function dictionary");
                return null;
            }
            COSArray bounds = getCOSArray(ASAtom.BOUNDS);
            subdomains = new ArrayList<>();
            subdomains.add(domain.at(0));
            if (bounds != null) {
                for (COSObject bound : bounds) {
                    subdomains.add(bound);
                }
            }
            subdomains.add(domain.at(1));
        }
        return subdomains;
    }

    public COSArray getEncode() {
        COSArray encode = getCOSArray(ASAtom.ENCODE);
        List<COSObject> subdomains = getSubdomains();
        if (encode == null) {
            List<COSObject> encodeFromSubdomains = new ArrayList<>();
            for (int i = 0; i < subdomains.size() - 1; ++i) {
                encodeFromSubdomains.add(subdomains.get(i));
                encodeFromSubdomains.add(subdomains.get(i + 1));
            }
            encode = new COSArray(encodeFromSubdomains);
        }
        return encode;
    }

    public List<PDFunction> getFunctions() {
        if (functions == null) {
            COSObject obj = getKey(ASAtom.FUNCTIONS);
            if (obj == null || obj.getType() != COSObjType.COS_ARRAY) {
                LOGGER.log(Level.WARNING, "Invalid Functions key value in Type 3 Function dictionary");
                return Collections.emptyList();
            }

            List<PDFunction> pdFunctions = new ArrayList<>();
            for (int i = 0; i < obj.size(); i++) {
                PDFunction function = PDFunction.createFunction(obj.at(i));
                if (function != null) {
                    pdFunctions.add(function);
                }
            }
            functions = Collections.unmodifiableList(pdFunctions);
        }
        return functions;
    }

    public int getIntervalNumber(COSObject x) {
        int functionsCount = getSubdomains().size() - 1;
        if (x.getReal().equals(getSubdomains().get(0).getReal())) {
            return 0;
        }
        for (int i = 0; i < functionsCount - 1; ++i) {
            if (x.getReal() >= getSubdomains().get(i).getReal() && x.getReal() < getSubdomains().get(i + 1).getReal()) {
                return i;
            }
        }
        return functionsCount - 1;
    }

    @Override
    public List<COSObject> getResult(List<COSObject> operands) {
        if (subdomains == null) {
            LOGGER.log(Level.WARNING, "Invalid subdomains in Type 3 Function dictionary");
            return null;
        }
        COSArray encode = getEncode();
        if (encode.size() < 2 * (subdomains.size() - 1)) {
            LOGGER.log(Level.WARNING, "Invalid Encode key value in Type 3 Function dictionary");
            return null;
        }
        if (operands.size() > 1) {
            LOGGER.log(Level.WARNING, "Too many operands. The first one will be chosen");
        }
        List<COSObject> operand = new ArrayList<>();
        operand.add(operands.get(0));
        operand = getValuesInIntervals(operand, domain);
        int i = getIntervalNumber(operand.get(0));
        operand.set(0, interpolate(operand.get(0), subdomains.get(i), subdomains.get(i + 1), encode.at(2 * i), encode.at(2 * i + 1)));
        return Collections.unmodifiableList(getValuesInIntervals(getFunctions().get(i).getResult(operand), getRange()));
    }
}
