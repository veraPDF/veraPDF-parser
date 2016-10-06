package org.verapdf.cos;

import org.verapdf.cos.visitor.ICOSVisitor;
import org.verapdf.cos.visitor.IVisitor;

/**
 * @author Timur Kamalov
 */
public class COSNull extends COSDirect {

    public static final COSNull NULL = new COSNull();

    public COSObjType getType() {
        return COSObjType.COS_NULL;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        return o instanceof COSNull;
    }
}
