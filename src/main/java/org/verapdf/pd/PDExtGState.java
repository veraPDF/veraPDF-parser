package org.verapdf.pd;

import org.verapdf.as.ASAtom;
import org.verapdf.cos.COSName;
import org.verapdf.cos.COSNumber;
import org.verapdf.cos.COSObjType;
import org.verapdf.cos.COSObject;

/**
 * @author Maksim Bezrukov
 */
public class PDExtGState extends PDResource {
    public PDExtGState(COSObject obj) {
        super(obj);
    }

    public Boolean getAlphaSourceFlag() {
        return getObject().getBooleanKey(ASAtom.AIS);
    }

    public Boolean getAutomaticStrokeAdjustment() {
        return getObject().getBooleanKey(ASAtom.SA);
    }

    public Boolean getStrokingOverprintControl() {
        return getObject().getBooleanKey(ASAtom.OP);
    }

    public Boolean getNonStrokingOverprintControl() {
        return getObject().getBooleanKey(ASAtom.OP_NS);
    }

    public Integer getOverprintMode() {
        return getObject().getIntegerKey(ASAtom.OPM).intValue();
    }

    public COSObject getCOSTR() {
        return getKey(ASAtom.TR);
    }

    public COSObject getCOSTR2() {
        return getKey(ASAtom.TR2);
    }

    public COSObject getCOSSMask() {
        return getKey(ASAtom.SMASK);
    }

    public COSObject getCOSBM() {
        return getKey(ASAtom.BM);
    }

    public Double getCA() {
        return getObject().getRealKey(ASAtom.CA);
    }

    public Double getCA_NS() {
        return getObject().getRealKey(ASAtom.CA_NS);
    }

    public COSName getCOSRenderingIntentName() {
        COSObject name = getKey(ASAtom.RI);
        if (name != null && name.getType() == COSObjType.COS_NAME) {
            return (COSName) name.get();
        }
        return null;
    }

    public COSNumber getCOSFontSize() {
        COSObject fontArray = getKey(ASAtom.FONT);
        if (fontArray != null && fontArray.getType() == COSObjType.COS_ARRAY) {
            COSObject res = fontArray.at(1);
            if (res != null && res.getType().isNumber()) {
                return (COSNumber) res.get();
            }
        }
        return null;
    }

    public PDHalftone getHalftone() {
        COSObject obj = getKey(ASAtom.HT);
        if (obj != null &&
                (obj.getType() == COSObjType.COS_NAME
                        || obj.getType() == COSObjType.COS_DICT
                        || obj.getType() == COSObjType.COS_STREAM)) {
            return new PDHalftone(obj);
        }
        return null;
    }

    public COSObject getHalftonePhase() {
        return getKey(ASAtom.HTP);
    }

//    TODO: implement me
//    public PDFont getFont() {
//
//    }
}
