package org.verapdf.pd.font;

import org.verapdf.as.ASAtom;
import org.verapdf.cos.COSArray;
import org.verapdf.cos.COSDictionary;
import org.verapdf.cos.COSName;
import org.verapdf.cos.COSObject;
import org.verapdf.pd.font.cmap.CMap;
import org.verapdf.pd.font.cmap.PDCMap;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Represents Type0 font on pd level.
 *
 * @author Sergey Shemyakov
 */
public class PDType0Font extends PDCIDFont {

    private static final Logger LOGGER = Logger.getLogger(PDType0Font.class.getCanonicalName());
    private static final String UCS2 = "UCS2";
    private static final String IDENTITY_H = "Identity-H";
    private static final String IDENTITY_V = "Identity-V";
    private static final String JAPAN_1 = "Japan1";
    private static final String KOREA_1 = "Korea1";
    private static final String GB_1 = "GB1";
    private static final String CNS_1 = "CNS1";
    private static final String ADOBE = "Adobe";

    private PDCMap pdcMap;
    private PDCMap ucsCMap;
    private COSDictionary type0FontDict;

    public PDType0Font(COSDictionary dictionary) {
        super(getDedcendantCOSDictionary(dictionary));
        type0FontDict = dictionary == null ?
                (COSDictionary) COSDictionary.construct().get() : dictionary;

        this.cMap = getCMap().getCMapFile();
    }

    public org.verapdf.pd.font.cmap.PDCMap getCMap() {
        if (this.pdcMap == null) {
            COSObject cMap = this.type0FontDict.getKey(ASAtom.ENCODING);
            if (!cMap.empty()) {
                org.verapdf.pd.font.cmap.PDCMap pdcMap = new org.verapdf.pd.font.cmap.PDCMap(cMap);
                this.pdcMap = pdcMap;
                return pdcMap;
            }
			return null;
        }
		return this.pdcMap;
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

    public COSDictionary getDescendantFont() {
        return getDedcendantCOSDictionary(this.type0FontDict);
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
        if (this.toUnicodeCMap == null) {
            this.toUnicodeCMap = new PDCMap(
                    this.type0FontDict.getKey(ASAtom.TO_UNICODE));
        }

        String unicode = super.toUnicode(code);
        if (unicode != null) {
            return unicode;
        }

        if (ucsCMap != null) {
            return ucsCMap.toUnicode(code);
        }

        if (IDENTITY_H.equals(pdcMap.getCMapName()) ||
                IDENTITY_V.equals(pdcMap.getCMapName())) {
            setUcsCMapFromIdentity(this.getCIDSystemInfo());
            if(this.ucsCMap == null) {
                LOGGER.log(Level.FINE, "Can't create toUnicode CMap from " + pdcMap.getCMapName());
                return null;
            }
			return ucsCMap.toUnicode(code);
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
		        this.ucsCMap = pdUCSCMap;
		        return ucsCMap.getUnicode(cid);
		    }
		    LOGGER.log(Level.FINE, "Can't load CMap " + ucsName);
		    return null;
		}
		LOGGER.log(Level.FINE, "Can't get CMap for font " + this.getName());
		return null;
    }

    private void setUcsCMapFromIdentity(PDCIDSystemInfo cidSystemInfo) {
        if (cidSystemInfo != null) {
            String registry = cidSystemInfo.getRegistry();
            if (ADOBE.equals(registry)) {
                String  ordering = cidSystemInfo.getOrdering();
                if(JAPAN_1.equals(ordering) || CNS_1.equals(ordering) ||
                        KOREA_1.equals(ordering) || GB_1.equals(ordering)) {
                    String ucsName = "Adobe-" + ordering + "-" + UCS2;
                    this.ucsCMap = new PDCMap(COSName.construct(ucsName));
                }
            }
        }
    }

    public void setFontProgramFromDescendant(PDCIDFont descendant) {
        this.fontProgram = descendant.fontProgram;
        this.isFontParsed = true;
    }

    public COSDictionary getType0FontDict() {
        return type0FontDict;
    }

    public int toCID(int code) {
        return this.pdcMap.getCMapFile().toCID(code);
    }
}
