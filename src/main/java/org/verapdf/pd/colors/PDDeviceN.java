package org.verapdf.pd.colors;

import org.verapdf.as.ASAtom;
import org.verapdf.cos.COSObjType;
import org.verapdf.cos.COSObject;
import org.verapdf.factory.colors.ColorSpaceFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Maksim Bezrukov
 */
public class PDDeviceN extends PDColorSpace {

    private final List<COSObject> names;

    public PDDeviceN(COSObject obj) {
        super(obj);
        this.names = parseNames(obj.at(1));
    }

    public List<COSObject> getNames() {
        return this.names;
    }

    public PDColorSpace getAlternateSpace() {
        return ColorSpaceFactory.getColorSpace(getObject().at(2));
    }

    public COSObject getTintTransform() {
        return getObject().at(3);
    }

    public COSObject getAttributes() {
        return getObject().at(4);
    }

    @Override
    public int getNumberOfComponents() {
        return names.size();
    }

    @Override
    public ASAtom getType() {
        return ASAtom.DEVICEN;
    }

    private static List<COSObject> parseNames(COSObject obj) {
        if (obj != null && obj.getType() == COSObjType.COS_ARRAY) {
            List<COSObject> names = new ArrayList<>(obj.size());
            for (int i = 0; i < obj.size(); ++i) {
                names.add(obj.at(i));
            }
            return Collections.unmodifiableList(names);
        }
        return Collections.emptyList();
    }
}
