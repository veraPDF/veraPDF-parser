package org.verapdf.pd.actions;

import org.verapdf.as.ASAtom;
import org.verapdf.cos.COSObject;

/**
 * @author Maksim Bezrukov
 */
public class PDAnnotationAdditionalActions extends PDAbstractAdditionalActions {

    public PDAnnotationAdditionalActions(COSObject obj) {
        super(obj);
    }

    public PDAction getE() {
        return getAction(ASAtom.E);
    }

    public PDAction getX() {
        return getAction(ASAtom.X);
    }

    public PDAction getD() {
        return getAction(ASAtom.D);
    }

    public PDAction getU() {
        return getAction(ASAtom.U);
    }

    public PDAction getFo() {
        return getAction(ASAtom.FOCUS_ABBREVIATION);
    }

    public PDAction getBl() {
        return getAction(ASAtom.BL_FOCUS);
    }

    public PDAction getPO() {
        return getAction(ASAtom.PO);
    }

    public PDAction getPC() {
        return getAction(ASAtom.PC);
    }

    public PDAction getPV() {
        return getAction(ASAtom.PV);
    }

    public PDAction getPI() {
        return getAction(ASAtom.PI);
    }
}
