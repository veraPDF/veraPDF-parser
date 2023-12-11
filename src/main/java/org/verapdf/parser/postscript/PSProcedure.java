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
import org.verapdf.cos.COSArray;
import org.verapdf.cos.COSObject;

import java.util.Map;
import java.util.Stack;

/**
 * Class represents PostScript procedure. Notice that when it is read from
 * PostScript program it is pushed to operand stack for later invocation.
 *
 * @author Sergey Shemyakov
 */
public class PSProcedure extends PSObject {

    private final COSArray procedure;

    public PSProcedure(COSArray procedure) {
        super(procedure);
        this.procedure = procedure;
    }

    public void executeProcedure(Stack<COSObject> operandStack,
                                 Map<ASAtom, COSObject> userDict) throws PostScriptException {
        for (COSObject obj : procedure) {
            PSObject.getPSObject(obj).execute(operandStack, userDict);
        }
    }

    @Override
    public void execute(Stack<COSObject> operandStack,
                        Map<ASAtom, COSObject> userDict) {
        operandStack.push(this);
    }

    public Stack<COSObject> modifiedExecuteProcedure(Stack<COSObject> operandStack,
                                                     Map<ASAtom, COSObject> userDict) throws PostScriptException {
        for (COSObject obj : procedure) {
            if (obj != null) {
                if (obj instanceof PSOperator) {
                    ((PSOperator) obj).execute(operandStack, userDict);
                } else {
                    operandStack.push(obj);
                }
            }
        }
        return operandStack;
    }
}
