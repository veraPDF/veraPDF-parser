package org.verapdf.pd.font;

import org.verapdf.as.ASAtom;
import org.verapdf.cos.COSArray;
import org.verapdf.cos.COSDictionary;
import org.verapdf.cos.COSObject;

/**
 * @author Sergey Shemyakov
 */
public class PDType0Font extends PDCIDFont {

    private org.verapdf.pd.font.cmap.PDCMap pdcMap;
    private COSDictionary cidSystemInfo;
    private COSDictionary type0FontDict;

    public PDType0Font(COSDictionary dictionary) {
        super(getDedcendantCOSDictionary(dictionary));
        type0FontDict = dictionary == null ? null :
                (COSDictionary) COSDictionary.construct().get();
    }

    public COSDictionary getCIDSystemInfo() {
        if (this.cidSystemInfo == null) {
            COSObject cidFontDictObj =
                    this.type0FontDict.getKey(ASAtom.DESCENDANT_FONTS).at(0);
            if (!cidFontDictObj.empty()) {
                COSDictionary cidFontDict = (COSDictionary) cidFontDictObj.getDirectBase();
                if (cidFontDict != null) {
                    COSDictionary cidSystemInfo = (COSDictionary)
                            cidFontDict.getKey(ASAtom.CID_SYSTEM_INFO).getDirectBase();
                    this.cidSystemInfo = cidSystemInfo;
                    return cidSystemInfo;
                }
            }
            return null;
        } else {
            return this.cidSystemInfo;
        }
    }

    public org.verapdf.pd.font.cmap.PDCMap getCMap() {
        if (this.pdcMap == null) {
            COSObject cMap = this.type0FontDict.getKey(ASAtom.ENCODING);
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

    private static COSDictionary getDedcendantCOSDictionary(COSDictionary dict) {
        if (dict != null) {
            COSArray array =
                    (COSArray) dict.getKey(ASAtom.DESCENDANT_FONTS).getDirectBase();
            if (array != null) {
                return (COSDictionary) array.at(0).getDirectBase();
            }
        }
        return null;
    }
}
