package org.verapdf.pd.font.truetype;

import org.verapdf.as.io.ASInputStream;
import org.verapdf.cos.COSObject;
import org.verapdf.pd.font.CIDToGIDMapping;
import org.verapdf.pd.font.FontProgram;
import org.verapdf.pd.font.cmap.CMap;

import java.io.IOException;

/**
 * Represents CIDFontType2 font program.
 *
 * @author Sergey Shemyakov
 */
public class CIDFontType2Program extends BaseTrueTypeProgram implements FontProgram {

    private CMap cMap;
    private CIDToGIDMapping cidToGID;

    /**
     * Constructor from font stream and encoding details.
     *
     * @param stream
     * @param cMap
     * @param cidToGID
     * @throws IOException
     */
    public CIDFontType2Program(ASInputStream stream, CMap cMap, COSObject cidToGID) throws IOException {
        super(stream);
        this.cMap = cMap;
        this.cidToGID = new CIDToGIDMapping(cidToGID);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public float getWidth(int code) {
        int cid = cMap.toCID(code);
        return this.getWidthWithCheck(cidToGID.getGID(cid));
    }

    @Override
    public float getWidth(String glyphName) {
        return 0;   // no need in this method
    }
}
