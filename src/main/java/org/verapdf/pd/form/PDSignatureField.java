package org.verapdf.pd.form;

import org.verapdf.as.ASAtom;
import org.verapdf.cos.COSDictionary;
import org.verapdf.cos.COSObject;
import org.verapdf.pd.PDSignature;

/**
 * Represents signature field.
 *
 * @author Sergey Shemyakov
 */
public class PDSignatureField extends PDFormField {

    public PDSignatureField(COSObject obj) {
        super(obj);
    }

    /**
     * @return digital signature contained in this signature field, or null if
     * digital signature can't be obtained.
     */
    public PDSignature getSignature() {
        COSDictionary sigDict = (COSDictionary)
                this.getObject().getKey(ASAtom.V).getDirectBase();
        return sigDict == null ? null : new PDSignature(sigDict);
    }

    /**
     * @return COSObject representing indirect reference to digital signature
     * contained in this signature field.
     */
    public COSObject getSignatureReference() {
        return this.getObject().getKey(ASAtom.V);
    }
}
