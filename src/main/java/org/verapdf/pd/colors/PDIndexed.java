package org.verapdf.pd.colors;

import org.verapdf.as.ASAtom;
import org.verapdf.as.io.ASInputStream;

/**
 * @author Maksim Bezrukov
 */
public class PDIndexed extends PDColorSpace {

    private final PDColorSpace base;
    private final Long hival;
    private final ASInputStream lookup;

    public PDIndexed(PDColorSpace base, Long hival, ASInputStream lookup) {
        this.base = base;
        this.hival = hival;
        this.lookup = lookup;
    }

    public PDColorSpace getBase() {
        return base;
    }

    public Long getHival() {
        return hival;
    }

    public ASInputStream getLookup() {
        return lookup;
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
