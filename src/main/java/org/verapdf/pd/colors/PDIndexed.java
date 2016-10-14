package org.verapdf.pd.colors;

import org.verapdf.as.ASAtom;
import org.verapdf.as.io.ASInputStream;
import org.verapdf.as.io.ASMemoryInStream;
import org.verapdf.cos.COSObjType;
import org.verapdf.cos.COSObject;
import org.verapdf.cos.COSStream;
import org.verapdf.factory.colors.ColorSpaceFactory;

/**
 * @author Maksim Bezrukov
 */
public class PDIndexed extends PDColorSpace {

    public PDIndexed(COSObject obj) {
        super(obj);
    }

    public PDColorSpace getBase() {
        return ColorSpaceFactory.getColorSpace(getObject().at(1));
    }

    public Long getHival() {
        return getObject().at(2).getInteger();
    }

    public ASInputStream getLookup() {
        COSObject object = getObject().at(3);
        if (object != null) {
            COSObjType type = object.getType();
            if (type == COSObjType.COS_STRING) {
                return new ASMemoryInStream(object.getString().getBytes());
            } else if (type == COSObjType.COS_STREAM) {
                return object.getData(COSStream.FilterFlags.DECODE);
            }
        }
        return null;
    }

    @Override
    public int getNumberOfComponents() {
        return 1;
    }

    @Override
    public ASAtom getType() {
        return ASAtom.INDEXED;
    }
}
