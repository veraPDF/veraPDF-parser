package org.verapdf.cos;

/**
 * Created by Timur on 12/18/2015.
 */
public class COSBoolean extends COSDirect {

    private boolean value;

    protected COSBoolean(final boolean initValue) {
        super();
        this.value = initValue;
    }

    public COSObjType getType() {
        return COSObjType.COSBooleanT;
    }

    public static COSObject construct(final boolean initValue) {
        return new COSObject(new COSBoolean(initValue));
    }

}
