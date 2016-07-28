package org.verapdf.font.cmap;

import org.verapdf.as.ASAtom;
import org.verapdf.cos.COSStream;

import java.io.IOException;

/**
 * Class represents CMap file embedded into COSStream.
 *
 * @author Sergey Shemyakov
 */
public class CMapFile {

    private CMap cMap;
    private COSStream parentStream;

    /**
     * Constructor from COSStream containing CMap.
     *
     * @param parentStream is CMap stream.
     */
    public CMapFile(COSStream parentStream) {
        this.parentStream = parentStream;
    }

    /**
     * @return the value of the WMode entry in the parent CMap dictionary.
     */
    public int getDictWMode() {
        return this.parentStream.getIntegerKey(ASAtom.W_MODE).intValue();
    }

    /**
     * @return the value of the WMode entry in the embedded CMap file.
     * @throws IOException if problem with parsing CMap file occurs.
     */
    public int getWMode() throws IOException {
        if (cMap == null) {
            CMapParser parser = new CMapParser(this.parentStream.getData(
                    COSStream.FilterFlags.DECODE)); // TODO: maybe add option "parse only WMode" to CMapParser
            parser.parse();
            cMap = parser.getCMap();
        }
        return cMap.getwMode();
    }
}
