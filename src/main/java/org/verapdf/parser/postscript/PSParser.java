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

    private Map<ASAtom, COSObject> userDict = new HashMap<>();
    private Stack<COSObject> operandStack = new Stack<>();

    public PSParser(ASInputStream fileStream) throws IOException {
        super(fileStream, true);
        userDict = new HashMap<>();
        operandStack = new Stack<>();
    }

    public void parse() throws PostScriptException {
        try {
            while (!this.source.isEOF()) {
                PSObject.getPSObject(nextObject()).execute(operandStack, userDict);
            }
        } catch (IOException e) {
            throw new PostScriptException("Can't parse post script stream", e);
        }
    }

    public COSObject getObjectFromUserDict(ASAtom key) {
        return userDict.get(key);
    }

}
