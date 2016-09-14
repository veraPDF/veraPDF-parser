package org.verapdf.pd.font;

import org.apache.log4j.Logger;
import org.verapdf.as.ASAtom;
import org.verapdf.as.io.ASInputStream;
import org.verapdf.cos.COSArray;
import org.verapdf.cos.COSDictionary;
import org.verapdf.cos.COSName;
import org.verapdf.cos.COSObject;
import org.verapdf.io.InternalInputStream;
import org.verapdf.pd.font.cmap.CMap;
import org.verapdf.pd.font.cmap.PDCMap;

import java.io.IOException;
import java.io.InputStream;

/**
 * Represents Type0 font on pd level.
 *
 * @author Sergey Shemyakov
 */
public class PDType0Font extends PDCIDFont {

    private static final Logger LOGGER = Logger.getLogger(PDType0Font.class);
    private static final String UCS2 = "UCS2";

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

    @Override
    public int readCode(InputStream stream) throws IOException {
        ASInputStream asInputStream = new InternalInputStream(stream);
        return this.pdcMap.getCMapFile().getCIDFromStream(asInputStream);
    }

    /**
     * This method maps character code to a Unicode value. Firstly it checks
     * toUnicode CMap, then it behaves like described in PDF32000_2008 9.10.2
     * "Mapping Character Codes to Unicode Values" for Type0 font.
     *
     * @param code is code for character.
     * @return unicode value.
     */
    @Override
    public String toUnicode(int code) {
        String unicode = super.toUnicode(code);
        if (unicode != null) {
            return unicode;
        }

        PDCMap pdcMap = this.getCMap();
        if (pdcMap != null && pdcMap.getCMapFile() != null) {
            int cid = pdcMap.getCMapFile().toCID(code);
            String registry = pdcMap.getRegistry();
            String ordering = pdcMap.getOrdering();
            String ucsName = registry + "-" + ordering + "-" + UCS2;
            PDCMap pdUCSCMap = new PDCMap(COSName.construct(ucsName));
            CMap ucsCMap = pdUCSCMap.getCMapFile();
            if (ucsCMap != null) {
                return ucsCMap.getUnicode(cid);
            }
            LOGGER.warn("Can't load CMap " + ucsName);
            return null;
        } else {
            LOGGER.warn("Can't get CMap for font " + this.getName());
            return null;
        }
    }

    public int toCID(int code) {
        return this.pdcMap.getCMapFile().toCID(code);
    }
}
