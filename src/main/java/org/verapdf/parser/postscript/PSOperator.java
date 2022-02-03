/**
 * This file is part of veraPDF Parser, a module of the veraPDF project.
 * Copyright (c) 2015, veraPDF Consortium <info@verapdf.org>
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
package org.verapdf.parser.postscript;

import org.verapdf.as.ASAtom;
import org.verapdf.cos.*;
import org.verapdf.pd.function.PSOperatorsConstants;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Stack;

/**
 * Represents executable PostScript operator.
 *
 * @author Sergey Shemyakov
 */
public class PSOperator extends PSObject {
    private Stack<COSObject> operandStack;
    private Map<ASAtom, COSObject> userDict;
    private final String operator;

    public PSOperator(COSName operator) {
        super(operator);
        this.operator = operator.getString();
    }

    public PSOperator(COSObject operator) {
        super(operator.get());
        this.operator = operator.getString();
    }

    public String getOperator() {
        return operator;
    }

    @Override
    public void execute(Stack<COSObject> operandStack,
                        Map<ASAtom, COSObject> userDict) throws PostScriptException {
        this.operandStack = operandStack;
        this.userDict = userDict;
        if (operator != null) {
            switch (operator) {
                case PSOperatorsConstants.LEFT_CURLY_BRACE:
                case PSOperatorsConstants.RIGHT_CURLY_BRACE:
                    break;
                // Conditional operators
                case PSOperatorsConstants.IF:
                    opIf();
                    break;
                case PSOperatorsConstants.IFELSE:
                    opIfElse();
                    break;

                // Operand Stack Manipulation Operators
                case PSOperatorsConstants.DUP:
                    operandStack.push(psCopyObject(operandStack.peek()));
                    break;
                case PSOperatorsConstants.EXCH:
                    COSObject obj1 = operandStack.pop();
                    COSObject obj2 = operandStack.pop();
                    operandStack.push(obj1);
                    operandStack.push(obj2);
                    break;
                case PSOperatorsConstants.POP:
                    operandStack.pop();
                    break;
                case PSOperatorsConstants.COPY:
                    copy();
                    break;
                case PSOperatorsConstants.INDEX:
                    index();
                    break;
                case PSOperatorsConstants.ROLL:
                    roll();
                    break;
                case PSOperatorsConstants.CLEAR:
                    this.operandStack.clear();
                    break;
                case PSOperatorsConstants.COUNT:
                    COSObject stackSize = COSInteger.construct(this.operandStack.size());
                    this.operandStack.push(stackSize);
                    break;
                case PSOperatorsConstants.MARK:
                    this.operandStack.push(PSStackMark.getInstance());
                    break;
                case PSOperatorsConstants.CLEARTOMARK:
                    COSObject topObject = this.operandStack.peek();
                    while (!operandStack.empty() && topObject != PSStackMark.getInstance()) {
                        operandStack.pop();
                        topObject = this.operandStack.peek();
                    }
                    break;
                case PSOperatorsConstants.COUNTTOMARK:
                    counttomark();
                    break;

                // Relational, boolean, and bitwise operator
                case PSOperatorsConstants.AND:
                case PSOperatorsConstants.OR:
                case PSOperatorsConstants.XOR:
                    executeOperatorOnTopTwoBooleans(operator);
                    break;
                case PSOperatorsConstants.NOT:
                    operandStack.push(COSBoolean.construct(!getTopBoolean().getBoolean()));
                    break;
                case PSOperatorsConstants.TRUE:
                    operandStack.push(COSBoolean.construct(true));
                    break;
                case PSOperatorsConstants.FALSE:
                    operandStack.push(COSBoolean.construct(false));
                    break;
                case PSOperatorsConstants.BITSHIFT:

                    // Arithmetic and Math Operators
                case PSOperatorsConstants.ABS:
                case PSOperatorsConstants.NEG:
                case PSOperatorsConstants.CEILING:
                case PSOperatorsConstants.FLOOR:
                case PSOperatorsConstants.ROUND:
                case PSOperatorsConstants.TRUNCATE:
                case PSOperatorsConstants.SQRT:
                case PSOperatorsConstants.COS:
                case PSOperatorsConstants.ATAN:
                case PSOperatorsConstants.SIN:
                case PSOperatorsConstants.EXP:
                case PSOperatorsConstants.CVI:
                case PSOperatorsConstants.CVR:
                case PSOperatorsConstants.LN:
                case PSOperatorsConstants.LOG:
                    executeOperatorOnOneTopNumber(operator);
                    break;
                case PSOperatorsConstants.ADD:
                case PSOperatorsConstants.DIV:
                case PSOperatorsConstants.IDIV:
                case PSOperatorsConstants.MOD:
                case PSOperatorsConstants.MUL:
                case PSOperatorsConstants.SUB:

                    // Relational, boolean, and bitwise operator
                case PSOperatorsConstants.EQ:
                case PSOperatorsConstants.NE:
                case PSOperatorsConstants.GT:
                case PSOperatorsConstants.GE:
                case PSOperatorsConstants.LT:
                case PSOperatorsConstants.LE:
                    executeOperatorOnTopTwoNumbers(operator);
                    break;

                //Dictionary Operators
                case PSOperatorsConstants.DICT:
                    // we use this for correct stack handling, no real dictionary
                    // processing is done
                    getTopNumber();
                    operandStack.push(COSDictionary.construct());
                    break;
                case PSOperatorsConstants.BEGIN:
                    if (operandStack.size() > 0) {
                        operandStack.pop();
                    }
                    break;
                case PSOperatorsConstants.LENGTH:
                    length();
                    break;
                case PSOperatorsConstants.DEF:
                    def();
                    break;
                case PSOperatorsConstants.LOAD:
                    load();
                    break;

                // Array Operators
                case PSOperatorsConstants.ARRAY:
                    array();
                    break;
                case PSOperatorsConstants.PUT:
                    put();
                    break;

                // Control Operators
                case PSOperatorsConstants.FOR:
                    opFor();
                    break;

                // PS Font Encoding
                case PSOperatorsConstants.STANDARD_ENCODING:
                    if (operandStack.empty()) {
                        break;
                    }
                    COSObject lastOperand = operandStack.peek();
                    if (lastOperand.getType() == COSObjType.COS_NAME
                            && lastOperand.getString().equals("Encoding")) {
                        operandStack.pop();
                        userDict.put(lastOperand.getName(), COSName.construct(ASAtom.STANDARD_ENCODING));
                    }
                    break;

                default:
                    COSObject dictEntry = userDict.get(ASAtom.getASAtom(operator));
                    if (dictEntry != null) {
                        PSObject.getPSObject(dictEntry).execute(operandStack, userDict);
                    }
            }
        }
    }

    private void opIfElse() throws PostScriptException {
        if (operandStack.size() >= 3) {
            COSObject falseProcedure = operandStack.pop();
            COSObject trueProcedure = operandStack.pop();
            COSObject bool = operandStack.pop();
                if (falseProcedure instanceof PSProcedure && trueProcedure instanceof PSProcedure &&
                        bool.getType() == COSObjType.COS_BOOLEAN) {
                    if (bool.getBoolean()) {
                        ((PSProcedure) trueProcedure).modifiedExecuteProcedure(operandStack, userDict);
                    } else {
                        ((PSProcedure) falseProcedure).modifiedExecuteProcedure(operandStack, userDict);
                    }
                } else {
                    throw new PostScriptException("Can't execute ifelse operator");
                }
        } else {
            throw new PostScriptException("No procedures for ifelse operator");
        }
    }

    private void opIf() throws PostScriptException {
        if (operandStack.size() >= 2) {
            COSObject procedure = operandStack.pop();
            COSObject bool = operandStack.pop();
            if (procedure instanceof PSProcedure && bool.getType() == COSObjType.COS_BOOLEAN) {
                if (bool.getBoolean()) {
                    ((PSProcedure) procedure).modifiedExecuteProcedure(operandStack, userDict);
                }
            } else {
                throw new PostScriptException("Can't execute if operator");
            }
        } else {
            throw new PostScriptException("No procedures for if operator");
        }
    }

    private void copy() throws PostScriptException {
        try {
            COSObject n = getTopNumber();
            int size = operandStack.size();
            if (size >= n.getInteger()) {
                List<COSObject> toCopy = operandStack.subList(size -
                        n.getInteger().intValue(), size);
                List<COSObject> toAppend = new ArrayList<>(toCopy.size());
                for (COSObject object : toCopy) {
                    toAppend.add(psCopyObject(object));
                }
                operandStack.addAll(toAppend);
            } else {
                throw new PostScriptException("Stack does not contain " + n.getInteger() + " elements to copy");
            }
        } catch (PostScriptException e) {
            throw new PostScriptException("Can't execute copy operator", e);
        }
    }

    private void index() throws PostScriptException {
        try {
            COSObject n = getTopNumber();
            if (operandStack.size() >= n.getInteger().intValue()) {
                COSObject toCopy = operandStack.get(operandStack.size() - n.getInteger().intValue() - 1);
                operandStack.push(psCopyObject(toCopy));
            } else {
                throw new PostScriptException("Stack does not contain " + n.getInteger() + " elements");
            }
        } catch (PostScriptException e) {
            throw new PostScriptException("Can't execute index operator", e);
        }
    }

    private void roll() throws PostScriptException {    // TODO: check this
        try {
            COSObject[] topTwoNumbers = getTopTwoNumbers();
            int j = topTwoNumbers[0].getInteger().intValue();
            int n = topTwoNumbers[1].getInteger().intValue();
            int size = operandStack.size();
            if (size >= n) {
                List<COSObject> lastElements = operandStack.subList(size - n, size);
                int splitPoint = n - 1 - ((j - 1) % n);
                operandStack.addAll(lastElements.subList(splitPoint - 1, size));
                operandStack.addAll(lastElements.subList(0, splitPoint - 1));
            } else {
                throw new PostScriptException("Stack has less than n elements");
            }
        } catch (PostScriptException e) {
            throw new PostScriptException("Can't execute roll operator", e);
        }
    }

    private void counttomark() {
        // check behaviour in case of missing mark
        long res = 0;
        for (int i = operandStack.size() - 1; i >= 0; --i) {
            if (operandStack.get(i) == PSStackMark.getInstance() || i == 0) {
                operandStack.push(COSInteger.construct(res));
                return;
            }
            res++;
        }
    }

    private void executeOperatorOnTopTwoNumbers(String operator) throws PostScriptException {
        try {
            COSObject[] topTwoNumbers = getTopTwoNumbers();
            COSObject res;
            switch (operator) {
                case PSOperatorsConstants.ADD:
                    res = COSReal.construct(topTwoNumbers[1].getReal() + topTwoNumbers[0].getReal());
                    break;
                case PSOperatorsConstants.DIV:
                    res = COSReal.construct(topTwoNumbers[1].getReal() / topTwoNumbers[0].getReal());
                    break;
                case PSOperatorsConstants.IDIV:
                    res = COSInteger.construct(topTwoNumbers[1].getInteger() / topTwoNumbers[0].getInteger());
                    break;
                case PSOperatorsConstants.MOD:
                    res = COSInteger.construct(topTwoNumbers[1].getInteger() % topTwoNumbers[0].getInteger());
                    break;
                case PSOperatorsConstants.MUL:
                    res = COSReal.construct(topTwoNumbers[1].getReal() * topTwoNumbers[0].getReal());
                    break;
                case PSOperatorsConstants.SUB:
                    res = COSReal.construct(topTwoNumbers[1].getReal() - topTwoNumbers[0].getReal());
                    break;
                case PSOperatorsConstants.EQ:
                    res = COSBoolean.construct(topTwoNumbers[1].getReal().equals(topTwoNumbers[0].getReal()));
                    break;
                case PSOperatorsConstants.NE:
                    res = COSBoolean.construct(!topTwoNumbers[1].getReal().equals(topTwoNumbers[0].getReal()));
                    break;
                case PSOperatorsConstants.GT:
                    res = COSBoolean.construct(topTwoNumbers[1].getReal() > topTwoNumbers[0].getReal());
                    break;
                case PSOperatorsConstants.GE:
                    res = COSBoolean.construct(topTwoNumbers[1].getReal() >= topTwoNumbers[0].getReal());
                    break;
                case PSOperatorsConstants.LT:
                    res = COSBoolean.construct(topTwoNumbers[1].getReal() < topTwoNumbers[0].getReal());
                    break;
                case PSOperatorsConstants.LE:
                    res = COSBoolean.construct(topTwoNumbers[1].getReal() <= topTwoNumbers[0].getReal());
                    break;
                default:
                    throw new PostScriptException("Unknown operator " + operator);
            }
            operandStack.push(res);
        } catch (PostScriptException e) {
            throw new PostScriptException("Can't execute " + operator + " operator", e);
        }
    }

    private void executeOperatorOnTopTwoBooleans(String operator) throws PostScriptException {
        try {
            COSObject[] topTwoBooleans = getTopTwoBooleans();
            COSObject res;
            switch (operator) {
                case PSOperatorsConstants.AND:
                    res = COSBoolean.construct(topTwoBooleans[1].getBoolean() && topTwoBooleans[0].getBoolean());
                    break;
                case PSOperatorsConstants.OR:
                    res = COSBoolean.construct(topTwoBooleans[1].getBoolean() || topTwoBooleans[0].getBoolean());
                    break;
                case PSOperatorsConstants.XOR:
                    res = COSBoolean.construct(topTwoBooleans[1].getBoolean() ^ topTwoBooleans[0].getBoolean());
                    break;
                default:
                    throw new PostScriptException("Unknown operator " + operator);
            }
            operandStack.push(res);
        } catch (PostScriptException e) {
            throw new PostScriptException("Can't execute " + operator + " operator", e);
        }
    }

    private void executeOperatorOnOneTopNumber(String operator) throws PostScriptException {
        try {
            COSObject argument = getTopNumber();
            COSObject res;
            switch (operator) {
                case PSOperatorsConstants.ABS:
                    res = COSReal.construct(Math.abs(argument.getReal()));
                    break;
                case PSOperatorsConstants.NEG:
                    res = COSReal.construct(-argument.getReal());
                    break;
                case PSOperatorsConstants.CEILING:
                    res = COSInteger.construct((long) Math.ceil(argument.getReal()));
                    break;
                case PSOperatorsConstants.FLOOR:
                    res = COSInteger.construct((long) Math.floor(argument.getReal()));
                    break;
                case PSOperatorsConstants.ROUND:
                    res = COSInteger.construct(Math.round(argument.getReal()));
                    break;
                case PSOperatorsConstants.TRUNCATE:
                    res = COSInteger.construct(argument.getReal().longValue());
                    break;
                case PSOperatorsConstants.SQRT:
                    res = COSReal.construct(Math.sqrt(argument.getReal()));
                    break;
                case PSOperatorsConstants.COS:
                    res = COSReal.construct(Math.cos(argument.getReal()));
                    break;
                case PSOperatorsConstants.ATAN:
                    res = COSReal.construct(Math.atan(argument.getReal()));
                    break;
                case PSOperatorsConstants.SIN:
                    res = COSReal.construct(Math.sin(argument.getReal()));
                    break;
                case PSOperatorsConstants.EXP:
                    res = COSReal.construct(Math.exp(argument.getReal()));
                    break;
                case PSOperatorsConstants.CVI:
                    res = COSInteger.construct(argument.getReal().intValue());
                    break;
                case PSOperatorsConstants.CVR:
                    res = COSReal.construct(argument.getReal());
                    break;
                case PSOperatorsConstants.LN:
                    res = COSReal.construct(Math.log(argument.getReal()));
                    break;
                case PSOperatorsConstants.LOG:
                    res = COSReal.construct(Math.log10(argument.getReal()));
                    break;
                case PSOperatorsConstants.BITSHIFT:
                    res = COSInteger.construct(argument.getInteger() >> 1);
                    break;
                default:
                    throw new PostScriptException("Unknown operator " + operator);
            }
            operandStack.push(res);
        } catch (PostScriptException e) {
            throw new PostScriptException("Can't execute " + operator + " operator", e);
        }
    }

    private void length() throws PostScriptException {
        COSObject topObject = popTopObject();
        if (topObject.getType().isDictionaryBased() || topObject.getType() == COSObjType.COS_ARRAY) {
            operandStack.push(COSInteger.construct(topObject.size()));
            return;
        }
        throw new PostScriptException("Can't execute length operator");
    }

    private void def() {
        if (operandStack.size() > 1) {
            COSObject value = operandStack.pop();
            COSObject key = operandStack.pop();
            COSObjType keyType = key.getType();
            if (keyType == COSObjType.COS_NAME || keyType == COSObjType.COS_STRING) {
                // any object can be key, but we support only COSName and COSString
                this.userDict.put(ASAtom.getASAtom(key.getString()), value);
            }
        }
    }

    private void load() throws PostScriptException {
        COSObject key = popTopObject();
        if (key.getType() == COSObjType.COS_STRING || key.getType() == COSObjType.COS_NAME) {
            ASAtom mapKey = ASAtom.getASAtom(key.getString());
            if (userDict.containsKey(mapKey)) {
                operandStack.push(userDict.get(mapKey));
            }
            return;
        }
        throw new PostScriptException("Can't execute load operator");
    }

    private void array() throws PostScriptException {
        try {
            int arraySize = getTopNumber().getInteger().intValue();
            COSObject array = COSArray.construct(arraySize);
            for (int i = 0; i < arraySize; i++) {
                array.add(COSObject.getEmpty());
            }
            this.operandStack.push(array);
        } catch (PostScriptException e) {
            throw new PostScriptException("Can't execute array operator", e);
        }
    }

    private void put() throws PostScriptException {
        try {
            if (operandStack.size() >= 3) {
                COSObject toPut = operandStack.pop();
                COSObject index = getTopNumber();
                COSObject array = operandStack.pop();
                if (array.getType() == COSObjType.COS_ARRAY) {
                    int intIndex = index.getInteger().intValue();
                    if (array.size() <= intIndex || intIndex < 0) {
                        throw new PostScriptException("Index greater than array size or less than 0");
                    }
                    array.remove(intIndex);
                    array.insert(intIndex, toPut);
                    return;
                }
            }
            throw new PostScriptException("Problem with stack");
        } catch (PostScriptException e) {
            throw new PostScriptException("Can't execute put operator", e);
        }
    }

    private void opFor() throws PostScriptException {
        try {
            if (operandStack.empty()) {
                throw new PostScriptException("Problem with stack");
            }
            COSObject proc = operandStack.pop();
            if (!(proc instanceof PSProcedure)) {
                throw new PostScriptException("Object is not a procedure");
            }
            COSObject limit = getTopNumber();
            COSObject increment = getTopNumber();
            COSObject initial = getTopNumber();
            for (long i = initial.getInteger(); i <= limit.getInteger();
                 i += increment.getInteger()) {
                operandStack.push(COSInteger.construct(i));
                ((PSProcedure) proc).executeProcedure(operandStack, userDict);
            }

        } catch (PostScriptException e) {
            throw new PostScriptException("Can't execute for operator", e);
        }
    }

    // keep in mind that first number to get from stack is result[0]
    // and the second one is result[1]
    private COSObject[] getTopTwoNumbers() throws PostScriptException {
        if (operandStack.size() > 1) {
            COSObject a = operandStack.pop();
            if (a.getType().isNumber()) {
                COSObject b = operandStack.pop();
                if (b.getType().isNumber()) {
                    return new COSObject[]{a, b};
                }
            }
        }
        throw new PostScriptException("Stack doesn't have two elements or top two elements are not two numbers");
    }

    private COSObject[] getTopTwoBooleans() throws PostScriptException {
        if (operandStack.size() > 1) {
            COSObject a = operandStack.pop();
            if (a.getType().isBoolean()) {
                COSObject b = operandStack.pop();
                if (b.getType().isBoolean()) {
                    return new COSObject[]{a, b};
                }
            }
        }
        throw new PostScriptException("Stack doesn't have two elements or top two elements are not two booleans");
    }

    private COSObject getTopNumber() throws PostScriptException {
        COSObject object = popTopObject();
        if (object.getType().isNumber()) {
            return object;
        }
        throw new PostScriptException("Stack is empty or top element is not a number");
    }

    private COSObject getTopBoolean() throws PostScriptException {
        COSObject object = popTopObject();
        if (object.getType().isBoolean()) {
            return object;
        }
        throw new PostScriptException("Stack is empty or top element is not a boolean");
    }

    private COSObject popTopObject() throws PostScriptException {
        if (!operandStack.empty()) {
            return operandStack.pop();
        }
        throw new PostScriptException("Operand stack is empty");
    }

    private static COSObject psCopyObject(COSObject toCopy) {
        switch (toCopy.getType()) {
            case COS_BOOLEAN:
                return COSBoolean.construct(toCopy.getBoolean());
            case COS_INTEGER:
                return COSInteger.construct(toCopy.getInteger());
            case COS_NAME:
                return COSName.construct(toCopy.getName());
            case COS_REAL:
                return COSReal.construct(toCopy.getReal());
            default:
                // In all other cases copied object share the same memory location
                return toCopy;
        }
    }
}
