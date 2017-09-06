package org.verapdf.parser.postscript;

import org.verapdf.as.ASAtom;
import org.verapdf.as.io.ASInputStream;
import org.verapdf.cos.COSObject;
import org.verapdf.parser.NotSeekableCOSParser;

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

    protected Map<ASAtom, COSObject> userDict = new HashMap<>();
    protected Stack<COSObject> operandStack = new Stack<>();

    public PSParser(ASInputStream fileStream) throws IOException {
        super(fileStream, true);
        userDict = new HashMap<>();
        operandStack = new Stack<>();
    }

    public COSObject getObjectFromUserDict(ASAtom key) {
        return userDict.get(key);
    }

}
