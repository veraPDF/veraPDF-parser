package org.verapdf.parser.postscript;

import org.verapdf.as.ASAtom;
import org.verapdf.cos.COSObject;

import java.util.Map;
import java.util.Stack;

/**
 * Class for executing PostScript objects.
 *
 * @author Sergey Shemyakov
 */
public class PSLiteralObject extends PSObject {

    private COSObject object;

    public PSLiteralObject(COSObject object) {
        super(object.get());
        this.object = object;
    }

    @Override
    public void execute(Stack<COSObject> operandStack,
                 Map<ASAtom, COSObject> userDict) throws PostScriptException {
        if (!object.empty()) {
            operandStack.push(this.object);
        }
    }
}
