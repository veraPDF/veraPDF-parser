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
    private String operator;

    public PSOperator(COSName operator) {
        super(operator);
        this.operator = operator.getString();
    }

    @Override
    public void execute(Stack<COSObject> operandStack,
                        Map<ASAtom, COSObject> userDict) throws PostScriptException {
        this.operandStack = operandStack;
        this.userDict = userDict;
        if (operator != null) {
            switch (operator) {
                // Operand Stack Manipulation Operators
                case "dup":
                    operandStack.push(psCopyObject(operandStack.peek()));
                    break;
                case "exch":
                    COSObject obj1 = operandStack.pop();
                    COSObject obj2 = operandStack.pop();
                    operandStack.push(obj1);
                    operandStack.push(obj2);
                    break;
                case "pop":
                    operandStack.pop();
                    break;
                case "copy":
                    copy();
                    break;
                case "index":
                    index();
                    break;
                case "roll":
                    roll();
                    break;
                case "clear":
                    this.operandStack.clear();
                    break;
                case "count":
                    COSObject stackSize = COSInteger.construct(this.operandStack.size());
                    this.operandStack.push(stackSize);
                    break;
                case "mark":
                    this.operandStack.push(PSStackMark.getInstance());
                    break;
                case "cleartomark":
                    COSObject topObject = this.operandStack.peek();
                    while (!operandStack.empty() && topObject != PSStackMark.getInstance()) {
                        operandStack.pop();
                        topObject = this.operandStack.peek();
                    }
                    break;
                case "counttomark":
                    counttomark();
                    break;

                // Arithmetic and Math Operators
                case "add":
                case "div":
                case "idiv":
                case "mod":
                case "mul":
                case "sub":
                    executeOperatorOnTopTwoNumbers(operator);
                    break;
                case "abs":
                case "neg":
                case "ceiling":
                case "floor":
                case "round":
                    executeOperatorOnOneTopNumber(operator);
                    break;

                //Dictionary Operators
                case "dict":
                    // we use this for correct stack handling, no real dictionary
                    // processing is done
                    getTopNumber();
                    operandStack.push(COSDictionary.construct());
                    break;
                case "begin":
                    if (operandStack.size() > 0) {
                        operandStack.pop();
                    }
                    break;
                case "length":
                    length();
                    break;
                case "def":
                    def();
                    break;
                case "load":
                    load();
                    break;

                // Array Operators
                case "array":
                    array();
                    break;
                case "put":
                    put();
                    break;

                // Control Operators
                case "for":
                    opFor();
                    break;

                // PS Font Encoding
                case "StandardEncoding":
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
                case "add":
                    res = COSReal.construct(topTwoNumbers[1].getReal() + topTwoNumbers[0].getReal());
                    break;
                case "div":
                    res = COSReal.construct(topTwoNumbers[1].getReal() / topTwoNumbers[0].getReal());
                    break;
                case "idiv":
                    res = COSInteger.construct(topTwoNumbers[1].getInteger() / topTwoNumbers[0].getInteger());
                    break;
                case "mod":
                    res = COSInteger.construct(topTwoNumbers[1].getInteger() % topTwoNumbers[0].getInteger());
                    break;
                case "mul":
                    res = COSReal.construct(topTwoNumbers[1].getReal() * topTwoNumbers[0].getReal());
                    break;
                case "sub":
                    res = COSReal.construct(topTwoNumbers[1].getReal() - topTwoNumbers[0].getReal());
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
                case "abs":
                    res = COSReal.construct(Math.abs(argument.getReal()));
                    break;
                case "neg":
                    res = COSReal.construct(-argument.getReal());
                    break;
                case "ceiling":
                    res = COSInteger.construct((long) Math.ceil(argument.getReal()));
                    break;
                case "floor":
                    res = COSInteger.construct((long) Math.floor(argument.getReal()));
                    break;
                case "round":
                    res = COSInteger.construct(Math.round(argument.getReal()));
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

    private COSObject getTopNumber() throws PostScriptException {
        COSObject object = popTopObject();
        if (object.getType().isNumber()) {
            return object;
        }
        throw new PostScriptException("Stack is empty or top element is not a number");
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
