package org.verapdf.pd;

import org.verapdf.cos.COSObjType;
import org.verapdf.cos.COSObject;
import org.verapdf.cos.COSStream;

/**
 * Class represents content stream that constructs and paints the glyph for
 * Type 3 font character.
 *
 * @author Sergey Shemyakov
 */
public class PDType3CharProc implements PDContentStream {

    private COSStream charStream;

    /**
     * Constructor from stream.
     *
     * @param charStream is COSStream, containing charProc content stream.
     */
    public PDType3CharProc(COSStream charStream) {
        this.charStream = charStream;
    }

    /**
     * @return COSStream, containing charProc content stream.
     */
    @Override
    public COSObject getContents() {
        return new COSObject(this.charStream);
    }

    /**
     * @param contents is COSStream, containing charProc content stream.
     */
    @Override
    public void setContents(COSObject contents) {
        if (contents != null && contents.getType() == COSObjType.COS_STREAM) {
            this.charStream = (COSStream) contents.get();
        }
    }
}
