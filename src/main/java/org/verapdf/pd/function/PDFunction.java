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
