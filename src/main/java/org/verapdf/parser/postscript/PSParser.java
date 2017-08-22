package org.verapdf.parser.postscript;

import org.verapdf.as.ASAtom;
import org.verapdf.as.io.ASInputStream;
import org.verapdf.cos.COSObject;
import org.verapdf.parser.NotSeekableCOSParser;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import java.util.logging.Logger;

/**
 * @author Sergey Shemyakov
 */
public class PSParser extends NotSeekableCOSParser {

    private static final Logger LOGGER = Logger.getLogger(PSParser.class.getCanonicalName());

    protected Map<ASAtom, COSObject> userDict = new HashMap<>();
    protected Stack<COSObject> operandStack = new Stack<>();

    public PSParser(ASInputStream fileStream) throws IOException {
        super(fileStream, true);
        userDict = new HashMap<>();
        operandStack = new Stack<>();
    }

    public void executeObject(COSObject object) throws PostScriptException {
        while (!this.source.isEOF()) {
            PSObject.getPSObject(object).execute(operandStack, userDict);
        }
    }

    public COSObject getObjectFromUserDict(ASAtom key) {
        return userDict.get(key);
    }

}
