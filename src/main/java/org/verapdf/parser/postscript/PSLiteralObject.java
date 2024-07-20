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
package org.verapdf.parser.postscript;

import org.verapdf.as.ASAtom;
import org.verapdf.cos.COSObject;

import java.util.Map;
import java.util.Stack;

/**
 * Class for literal PostScript objects. These objects are just pushed to
 * operand stack while executed.
 *
 * @author Sergey Shemyakov
 */
public class PSLiteralObject extends PSObject {

    private final COSObject object;

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
