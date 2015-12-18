package org.verapdf.cos;

/**
 * Created by Timur on 12/18/2015.
 */
public class COSNull extends COSDirect {

    public COSObjType getType() {
        return COSObjType.COSNullT;
    }

    public static COSObject construct() {
        return new COSObject(new COSNull());
    }

}
