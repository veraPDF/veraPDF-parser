package org.verapdf.pd.colors;

import org.verapdf.cos.COSObject;
import org.verapdf.factory.colors.ColorSpaceFactory;
import org.verapdf.pd.PDResources;

/**
 * Basse class for special color spaces.
 *
 * @author Sergey Shemyakov
 */
public abstract class PDSpecialColorSpace extends PDColorSpace {

    protected PDColorSpace baseColorSpace;

    public PDSpecialColorSpace(COSObject obj, PDResources resources) {
        super(obj);
        this.baseColorSpace = ColorSpaceFactory.getColorSpace(
                getBaseColorSpaceObject(), resources);
    }

    abstract COSObject getBaseColorSpaceObject();
}
