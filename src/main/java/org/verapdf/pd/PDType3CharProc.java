package org.verapdf.pd;

import org.verapdf.cos.COSObjType;
import org.verapdf.cos.COSObject;

/**
 * Class represents content stream that constructs and paints the glyph for
 * Type 3 font character.
 *
 * @author Sergey Shemyakov
 */
public class PDType3CharProc extends PDObject implements PDContentStream {

    /**
     * Constructor from stream.
     *
     * @param charStream is COSObject containing charProc content stream.
     */
    public PDType3CharProc(COSObject charStream) {
        super(charStream);
    }

    /**
     * @return COSStream, containing charProc content stream.
     */
    @Override
    public COSObject getContents() {
        return getObject();
    }

    /**
     * @param contents is COSStream, containing charProc content stream.
     */
    @Override
    public void setContents(COSObject contents) {
        if (contents != null && contents.getType() == COSObjType.COS_STREAM) {
            setObject(contents);
        }
    }
}
