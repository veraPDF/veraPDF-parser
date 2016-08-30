package org.verapdf.pd;

import org.verapdf.as.ASAtom;
import org.verapdf.cos.COSObjType;
import org.verapdf.cos.COSObject;

/**
 * Represents CMap on PD layer.
 * //TODO: documentation
 *
 * @author Sergey Shemyakov
 */
public class PDCMap {

    private COSObject cMap;

    public PDCMap(COSObject cMap) {
        this.cMap = cMap == null ? COSObject.getEmpty() : cMap;
    }

    public String getCMapName() {
        if (this.cMap.getType() == COSObjType.COS_NAME) {
            return cMap.getString();
        }
        if (this.cMap.getType() == COSObjType.COS_STREAM) {
            COSObject cMapName = this.cMap.getKey(ASAtom.CMAPNAME);
            if (cMapName != COSObject.getEmpty()) {
                return cMapName.getString();
            }
        }
        return "";
    }

    public COSObject getcMap() {
        return cMap;
    }
}
