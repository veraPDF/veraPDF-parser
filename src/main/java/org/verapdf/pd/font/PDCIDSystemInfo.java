package org.verapdf.pd.font;

import org.verapdf.as.ASAtom;
import org.verapdf.cos.COSObject;
import org.verapdf.pd.PDObject;

/**
 * Represents CIDSystemInfo dictionary in CID fonts.
 *
 * @author Sergey Shemyakov
 */
public class PDCIDSystemInfo extends PDObject {

    public PDCIDSystemInfo(COSObject obj) {
        super(obj);
    }

    /**
     * @return a string identifying the issuer of the character collection.
     */
    public String getRegistry() {
        return getStringKey(ASAtom.REGISTRY);
    }

    /**
     * @return a string that uniquely names the character collection within the
     * specified registry.
     */
    public String getOrdering() {
        return getStringKey(ASAtom.ORDERING);
    }

    /**
     * @return the supplement number of the character collection.
     */
    public Long getSupplement() {
        return getIntegerKey(ASAtom.SUPPLEMENT);
    }
}
