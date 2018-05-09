package org.verapdf.parser.postscript;

import org.verapdf.as.ASAtom;
import org.verapdf.cos.*;

import java.util.Map;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This is the base class for PostScript object.
 *
 * @author Sergey Shemyakov
 */
public abstract class PSObject extends COSObject {

    private static final Logger LOGGER = Logger.getLogger(PSObject.class.getCanonicalName());

    protected PSObject(COSBase base) {
        super(base);
    }

    /**
     * Executes PostScript object. For literal objects this execution means
     * pushing object to operand stack, for operator and procedure objects
     * execution mean execution of this operator or procedure.
     *
     * @param operandStack is stack for PostScript operands (see PostScript
     *                     specification for further information).
     * @param userDict is a dictionary that stores all key-value associated pair
     *                 encountered during PostScript parsing. Full PostScript
     *                 parser needs a dict stack, our implementation has only one
     *                 dictionary.
     */
    public abstract void execute(Stack<COSObject> operandStack,
                          Map<ASAtom, COSObject> userDict) throws PostScriptException;

    /**
     * Constructs PostScript object from COS object.
     */
    public static PSObject getPSObject(COSObject obj) {
        return getPSObject(obj, false);
    }

    public static PSObject getPSObject(COSObject obj, boolean isExecutable) {
        if (obj instanceof PSObject) {
            return (PSObject) obj;
        }
        COSObjType type = obj.getType();
        if (type == COSObjType.COS_NAME && isExecutable) {
            return new PSOperator((COSName) obj.get());
        } else if (type == COSObjType.COS_ARRAY && isExecutable) {
            return new PSProcedure((COSArray) obj.get());
        } else if (type.isNumber() || type == COSObjType.COS_STRING ||
                type == COSObjType.COS_BOOLEAN || type == COSObjType.COS_DICT ||
                type == COSObjType.COS_NAME || type == COSObjType.COS_ARRAY) {
            return new PSLiteralObject(obj);
        } else {
            LOGGER.log(Level.FINE, "Can't get PSObject for COSType " + type);
            return new PSLiteralObject(obj);
        }
    }
}
