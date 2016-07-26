package org.verapdf.pd.colors;

import org.verapdf.as.ASAtom;
import org.verapdf.cos.COSObject;

import java.util.Collections;
import java.util.List;

/**
 * @author Maksim Bezrukov
 */
public class PDDeviceN extends PDColorSpace {

    private final List<ASAtom> names;
    private final PDColorSpace alternateSpace;
    private final COSObject tintTransform;
    private final COSObject attributes;

    public PDDeviceN(List<ASAtom> names, PDColorSpace alternateSpace, COSObject tintTransform, COSObject attributes) {
        this.names = names;
        this.alternateSpace = alternateSpace;
        this.tintTransform = tintTransform;
        this.attributes = attributes;
    }

    public List<ASAtom> getNames() {
        return names == null ? null : Collections.unmodifiableList(names);
    }

    public PDColorSpace getAlternateSpace() {
        return alternateSpace;
    }

    public COSObject getTintTransform() {
        return tintTransform;
    }

    public COSObject getAttributes() {
        return attributes;
    }

    @Override
    public int getNumberOfComponents() {
        return names == null ? 0 : names.size();
    }

    @Override
    public ASAtom getType() {
        return ASAtom.DEVICEN;
    }
}
