package org.verapdf.pd.font;

import org.verapdf.as.ASAtom;
import org.verapdf.cos.COSDictionary;
import org.verapdf.cos.COSObject;

/**
 * @author Sergey Shemyakov
 */
public class PDType0Font extends PDFont {

    private org.verapdf.pd.font.cmap.PDCMap pdcMap;
    private COSDictionary cidSystemInfo;

    public PDType0Font(COSDictionary dictionary) {
        super(dictionary);
    }

    public COSDictionary getCIDSystemInfo() {
        if (this.cidSystemInfo == null) {
            COSDictionary cidFontDict = (COSDictionary)
                    dictionary.getKey(ASAtom.DESCENDANT_FONTS).get();
            if (cidFontDict != null) {
                COSDictionary cidSystemInfo = (COSDictionary)
                        cidFontDict.getKey(ASAtom.CID_SYSTEM_INFO).get();
                this.cidSystemInfo = cidSystemInfo;
                return cidSystemInfo;
            }
            return null;
        } else {
            return this.cidSystemInfo;
        }
    }

    public org.verapdf.pd.font.cmap.PDCMap getCMap() {
        if (this.pdcMap == null) {
            COSObject cMap = this.dictionary.getKey(ASAtom.ENCODING);
            if (!cMap.empty()) {
                org.verapdf.pd.font.cmap.PDCMap pdcMap = new org.verapdf.pd.font.cmap.PDCMap(cMap);
                this.pdcMap = pdcMap;
                return pdcMap;
            } else {
                return null;
            }
        } else {
            return this.pdcMap;
        }
    }
}
