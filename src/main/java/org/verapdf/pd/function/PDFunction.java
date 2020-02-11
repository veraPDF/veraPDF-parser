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
package org.verapdf.pd.function;

import org.verapdf.as.ASAtom;
import org.verapdf.as.io.ASInputStream;
import org.verapdf.cos.COSObjType;
import org.verapdf.cos.COSObject;
import org.verapdf.cos.COSStream;
import org.verapdf.parser.FunctionParser;
import org.verapdf.pd.PDObject;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PDFunction extends PDObject {

    private static final Logger LOGGER = Logger.getLogger(PDFunction.class.getCanonicalName());

    private FunctionParser parser;

    protected PDFunction(COSObject obj) {
       super(obj);
   }

   public static PDFunction createFunction(COSObject obj) {
        if (obj == null || !obj.getType().isDictionaryBased()) {
            return null;
        }

       Long functionType = obj.getIntegerKey(ASAtom.FUNCTION_TYPE);

        if (functionType == null) {
            LOGGER.log(Level.WARNING,"FunctionType is missing or not a number");
            return new PDFunction(obj);
        }

        switch (functionType.intValue()) {
            case 3:
                return new PDType3Function(obj);
            default:
                return new PDFunction(obj);
        }
   }

    public List<COSObject> getOperators() {
        COSObject obj = this.getObject();
        if (obj.getType() != COSObjType.COS_STREAM) {
            return Collections.emptyList();
        }
        if (this.parser == null) {
            try {
                parseStream();
            } catch (IOException e) {
                LOGGER.log(Level.WARNING,"Can not parse function", e);
                return Collections.emptyList();
            }
        }
        return Collections.unmodifiableList(parser.getOperators());
    }

    private void parseStream() throws IOException{
        try (ASInputStream functionStream = getObject().getData(COSStream.FilterFlags.DECODE)) {
            this.parser = new FunctionParser(functionStream);
            this.parser.parse();
        } finally {
            if (this.parser != null) {
                this.parser.closeInputStream();
            }
        }
    }

    public Long getFunctionType() {
        return getObject().getIntegerKey(ASAtom.FUNCTION_TYPE);
    }

}
