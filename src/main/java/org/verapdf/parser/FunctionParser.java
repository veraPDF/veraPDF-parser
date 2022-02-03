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
package org.verapdf.parser;

import org.verapdf.cos.COSInteger;
import org.verapdf.cos.COSName;
import org.verapdf.cos.COSObject;
import org.verapdf.cos.COSReal;
import org.verapdf.parser.postscript.PSOperator;
import org.verapdf.pd.function.PSOperatorsConstants;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FunctionParser extends BaseParser {

    private static final Logger LOGGER = Logger.getLogger(FunctionParser.class.getCanonicalName());

    public static final Set<String> FUNCTION_KEYWORDS;
    static {
        Set<String> tempSet = new HashSet<>();
        tempSet.add(PSOperatorsConstants.ABS);
        tempSet.add(PSOperatorsConstants.CVI);
        tempSet.add(PSOperatorsConstants.FLOOR);
        tempSet.add(PSOperatorsConstants.MOD);
        tempSet.add(PSOperatorsConstants.SIN);
        tempSet.add(PSOperatorsConstants.ADD);
        tempSet.add(PSOperatorsConstants.CVR);
        tempSet.add(PSOperatorsConstants.IDIV);
        tempSet.add(PSOperatorsConstants.MUL);
        tempSet.add(PSOperatorsConstants.SQRT);
        tempSet.add(PSOperatorsConstants.ATAN);
        tempSet.add(PSOperatorsConstants.DIV);
        tempSet.add(PSOperatorsConstants.LN);
        tempSet.add(PSOperatorsConstants.NEG);
        tempSet.add(PSOperatorsConstants.SUB);
        tempSet.add(PSOperatorsConstants.CEILING);
        tempSet.add(PSOperatorsConstants.EXP);
        tempSet.add(PSOperatorsConstants.LOG);
        tempSet.add(PSOperatorsConstants.ROUND);
        tempSet.add(PSOperatorsConstants.TRUNCATE);
        tempSet.add(PSOperatorsConstants.COS);
        tempSet.add(PSOperatorsConstants.AND);
        tempSet.add(PSOperatorsConstants.FALSE);
        tempSet.add(PSOperatorsConstants.LE);
        tempSet.add(PSOperatorsConstants.NOT);
        tempSet.add(PSOperatorsConstants.TRUE);
        tempSet.add(PSOperatorsConstants.BITSHIFT);
        tempSet.add(PSOperatorsConstants.GE);
        tempSet.add(PSOperatorsConstants.LT);
        tempSet.add(PSOperatorsConstants.OR);
        tempSet.add(PSOperatorsConstants.XOR);
        tempSet.add(PSOperatorsConstants.EQ);
        tempSet.add(PSOperatorsConstants.GT);
        tempSet.add(PSOperatorsConstants.NE);
        tempSet.add(PSOperatorsConstants.IF);
        tempSet.add(PSOperatorsConstants.IFELSE);
        tempSet.add(PSOperatorsConstants.COPY);
        tempSet.add(PSOperatorsConstants.EXCH);
        tempSet.add(PSOperatorsConstants.POP);
        tempSet.add(PSOperatorsConstants.DUP);
        tempSet.add(PSOperatorsConstants.INDEX);
        tempSet.add(PSOperatorsConstants.ROLL);
        tempSet.add(PSOperatorsConstants.LEFT_CURLY_BRACE);
        tempSet.add(PSOperatorsConstants.RIGHT_CURLY_BRACE);

        FUNCTION_KEYWORDS = Collections.unmodifiableSet(tempSet);
    }

    private List<COSObject> operators = new ArrayList<>();

    public FunctionParser(InputStream functionStream) throws IOException {
        super(functionStream);
    }

    public void parse() throws IOException {

        initializeToken();

        skipSpaces(true);

        while (getToken().type != Token.Type.TT_EOF) {
            nextToken();
            processToken();
        }
    }

    private void processToken() {
        switch (this.getToken().type) {
            case TT_NONE:
            case TT_EOF:
                break;
            case TT_KEYWORD:
                if (!FUNCTION_KEYWORDS.contains(this.getToken().getValue())) {
                    LOGGER.log(Level.WARNING, "Invalid keyword in Function");
                } else {
                    operators.add(new PSOperator(COSName.construct(this.getToken().getValue())));
                }
                break;
            case TT_INTEGER:
                operators.add(COSInteger.construct(this.getToken().integer));
                break;
            case TT_REAL:
                operators.add(COSReal.construct(this.getToken().real));
                break;
            default:
                LOGGER.log(Level.WARNING, "Invalid object type in Function");
                break;
        }
    }

    public List<COSObject> getOperators() {
        return Collections.unmodifiableList(operators);
    }
}
