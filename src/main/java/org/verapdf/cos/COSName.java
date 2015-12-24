package org.verapdf.cos;

import org.verapdf.as.ASAtom;

/**
 * Created by Timur on 12/18/2015.
 */
public class COSName extends COSDirect {

    private ASAtom value;

    protected COSName(final ASAtom value) {
        super();
        this.value = value;
    }

    protected COSName(final String value) {
        this(new ASAtom(value));
    }

    public static COSObject construct(final ASAtom value) {
        return new COSObject(new COSName(value));
    }

    public static COSObject construct(final String value) {
        return new COSObject(new COSName(value));
    }

    public COSObjType getType() {
        return COSObjType.COSNameT;
    }

    public ASAtom getName() {
        return this.value;
    }

}
