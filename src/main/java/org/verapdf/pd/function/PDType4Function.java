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

import org.verapdf.as.io.ASInputStream;
import org.verapdf.cos.COSArray;
import org.verapdf.cos.COSObjType;
import org.verapdf.cos.COSObject;
import org.verapdf.cos.COSStream;
import org.verapdf.parser.FunctionParser;
import org.verapdf.parser.postscript.PSOperator;
import org.verapdf.parser.postscript.PSProcedure;
import org.verapdf.parser.postscript.PostScriptException;

import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PDType4Function extends PDFunction {
    private List<COSObject> operators;
    private List<COSObject> modifiedOperators;
    private static final Logger LOGGER = Logger.getLogger(PDType4Function.class.getCanonicalName());
    private FunctionParser parser;

    protected PDType4Function(COSObject obj) {
        super(obj);
    }

    public List<COSObject> getOperators() {
        if (operators == null) {
            operators = parseOperators();
        }
        return operators;
    }

    private void parseStream() throws IOException {
        try (ASInputStream functionStream = getObject().getData(COSStream.FilterFlags.DECODE)) {
            this.parser = new FunctionParser(functionStream, getObject().getKey());
            this.parser.parse();
        } finally {
            if (this.parser != null) {
                this.parser.closeInputStream();
            }
        }
    }

    public List<COSObject> parseOperators() {
        COSObject obj = this.getObject();
        if (obj.getType() != COSObjType.COS_STREAM) {
            return Collections.emptyList();
        }
        if (this.parser == null) {
            try {
                parseStream();
            } catch (IOException e) {
                LOGGER.log(Level.WARNING, "Can not parse function", e);
                return Collections.emptyList();
            }
        }
        return Collections.unmodifiableList(parser.getOperators());
    }

    public void setOperators(List<COSObject> operators) {
        this.operators = operators;
        this.modifiedOperators = null;
    }

    private List<COSObject> getOperatorsWithProcedures() {
        if (modifiedOperators == null) {
            modifiedOperators = new ArrayList<>();
            Iterator<COSObject> ops = getOperators().iterator();
            if (PSOperatorsConstants.LEFT_CURLY_BRACE.equals(getOperators().get(0).getString())) {
                ops.next();
            }
            while (ops.hasNext()) {
                COSObject obj = ops.next();
                if (obj != null) {
                    if (obj instanceof PSOperator &&
                            PSOperatorsConstants.LEFT_CURLY_BRACE.equals(((PSOperator) obj).getOperator())) {
                        recursiveProcedure(ops, modifiedOperators);
                    } else {
                        modifiedOperators.add(obj);
                    }
                }
            }
        }
        return modifiedOperators;
    }

    private void recursiveProcedure(Iterator<COSObject> ops, List<COSObject> modifiedOperators) {
        List<COSObject> proc = new ArrayList<>();
        while (ops.hasNext()) {
            COSObject obj = ops.next();
            if (obj instanceof PSOperator && PSOperatorsConstants.RIGHT_CURLY_BRACE.equals(((PSOperator) obj).getOperator())) {
                break;
            }
            if (obj instanceof PSOperator && PSOperatorsConstants.LEFT_CURLY_BRACE.equals(((PSOperator) obj).getOperator())) {
                recursiveProcedure(ops, proc);
            }
            proc.add(obj);
        }
        modifiedOperators.add(new PSProcedure(new COSArray(proc)));
    }

    @Override
    public List<COSObject> getResult(List<COSObject> operands) {
        try {
            Stack<COSObject> operandStack = new Stack<>();
            operandStack.addAll(getValuesInIntervals(operands, getDomain()));
            for (COSObject obj : getOperatorsWithProcedures()) {
                if (obj != null) {
                    if (obj instanceof PSOperator) {
                        ((PSOperator) obj).execute(operandStack, new HashMap<>());
                    } else {
                        operandStack.push(obj);
                    }
                }
            }
            List<COSObject> operandsInRange = new ArrayList<>(operandStack);
            return Collections.unmodifiableList(getValuesInIntervals(operandsInRange, getRange()));
        } catch (PostScriptException e) {
            LOGGER.log(Level.WARNING, "Invalid operators stream", e);
            return null;
        }
    }
}
