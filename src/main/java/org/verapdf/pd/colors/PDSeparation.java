package org.verapdf.pd.colors;

import org.verapdf.as.ASAtom;
import org.verapdf.cos.COSObject;
import org.verapdf.factory.colors.ColorSpaceFactory;

/**
 * @author Maksim Bezrukov
 */
public class PDSeparation extends PDColorSpace {

    public PDSeparation(COSObject obj) {
        super(obj);
    }

    public COSObject getColorantName() {
        return getObject().at(1);
    }

    public PDColorSpace getAlternate() {
        return ColorSpaceFactory.getColorSpace(getObject().at(2));
    }

    public COSObject getTintTransform() {
        return getObject().at(3);
    }

    @Override
    public int getNumberOfComponents() {
        return 1;
    }

    @Override
    public ASAtom getType() {
        return ASAtom.SEPARATION;
    }
}
