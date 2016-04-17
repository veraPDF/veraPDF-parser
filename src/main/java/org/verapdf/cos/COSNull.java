package org.verapdf.cos;

import org.verapdf.cos.visitor.ICOSVisitor;
import org.verapdf.cos.visitor.IVisitor;

/**
 * Created by Timur on 12/18/2015.
 */
public class COSNull extends COSDirect {

    public static final COSNull NULL = new COSNull();

    public COSObjType getType() {
        return COSObjType.COSNullT;
    }

    public static COSObject construct() {
        return new COSObject(new COSNull());
    }

    public void accept(final IVisitor visitor) {
        visitor.visitFromNull(this);
    }

    public Object accept(final ICOSVisitor visitor) {
        return visitor.visitFromNull(this);
    }

}
