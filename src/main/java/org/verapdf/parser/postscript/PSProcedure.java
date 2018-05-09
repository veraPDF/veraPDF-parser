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

    private COSArray procedure;

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
                        Map<ASAtom, COSObject> userDict) throws PostScriptException {
        operandStack.push(this);
    }
}
