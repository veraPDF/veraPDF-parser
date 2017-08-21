package org.verapdf.parser.postscript;

import org.verapdf.cos.COSObject;

/**
 * Object represents operand stack mark.
 *
 * @author Sergey Shemyakov
 */
public class PSStackMark extends COSObject {

    private static final PSStackMark STACK_MARK_INSTANCE = new PSStackMark();

    private PSStackMark() {
    }

    public static PSStackMark getInstance() {
        return STACK_MARK_INSTANCE;
    }
}
