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
import org.verapdf.as.io.ASInputStream;
import org.verapdf.cos.COSName;
import org.verapdf.cos.COSObject;
import org.verapdf.parser.NotSeekableBaseParser;
import org.verapdf.parser.NotSeekableCOSParser;
import org.verapdf.pd.function.PSOperatorsConstants;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

/**
 * PostScript parser that holds operand stack and user dictionary.
 *
 * @author Sergey Shemyakov
 */
public class PSParser extends NotSeekableCOSParser {

    protected final Map<ASAtom, COSObject> userDict = new HashMap<>();
    protected final Stack<COSObject> operandStack = new Stack<>();

    public PSParser(ASInputStream fileStream) throws IOException {
        super(fileStream, true);
    }

    public PSParser(NotSeekableBaseParser baseParser) {
        super(baseParser);
    }

    public COSObject getObjectFromUserDict(ASAtom key) {
        return userDict.get(key);
    }

    @Override
    protected COSObject getDictionary() {
        this.flag = true;
        return COSName.construct(PSOperatorsConstants.LEFT_ANGLE_BRACES);
    }

    @Override
    protected COSObject getCloseDictionary() {
        return COSName.construct(PSOperatorsConstants.RIGHT_ANGLE_BRACES);
    }
}
