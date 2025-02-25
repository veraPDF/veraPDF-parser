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
            return new PSOperator((COSName) obj.getDirectBase());
        } else if (type == COSObjType.COS_ARRAY && isExecutable) {
            return new PSProcedure((COSArray) obj.getDirectBase());
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
