package org.verapdf.parser.postscript;

import org.verapdf.as.ASAtom;
import org.verapdf.cos.*;

import java.util.Map;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class represents executable PostScript object.
 *
 * @author Sergey Shemyakov
 */
public abstract class PSObject extends COSObject {

    private static final Logger LOGGER = Logger.getLogger(PSObject.class.getCanonicalName());

    public PSObject(COSBase base) {
        super(base);
    }

    abstract void execute(Stack<COSObject> operandStack,
                          Map<ASAtom, COSObject> userDict) throws PostScriptException;

    public static PSObject getPSObject(COSObject obj) {
        return getPSObject(obj, false);
    }

    // full PostScript parser needs a dict stack, our implementation has only one dict
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
            // TODO: change SEVERE to FINE after testing
            LOGGER.log(Level.SEVERE, "Can't get PSObject for COSType " + type);
            return new PSLiteralObject(obj);
        }
    }
}
