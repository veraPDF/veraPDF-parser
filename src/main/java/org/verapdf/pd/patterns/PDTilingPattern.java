package org.verapdf.pd.patterns;

import org.apache.log4j.Logger;
import org.verapdf.as.ASAtom;
import org.verapdf.cos.COSObjType;
import org.verapdf.cos.COSObject;
import org.verapdf.pd.PDContentStream;
import org.verapdf.pd.PDResources;
import org.verapdf.tools.TypeConverter;

/**
 * @author Maksim Bezrukov
 */
public class PDTilingPattern extends PDPattern implements PDContentStream {

    private static final Logger LOGGER = Logger.getLogger(PDTilingPattern.class);

    public PDTilingPattern(COSObject obj) {
        super(obj);
    }

    @Override
    public int getPatternType() {
        return PDPattern.TYPE_TILING_PATTERN;
    }

    @Override
    public COSObject getContents() {
        return super.getObject();
    }

    @Override
    public void setContents(COSObject contents) {
        super.setObject(contents);
    }

    public Long getPaintType() {
        return getObject().getIntegerKey(ASAtom.PAINT_TYPE);
    }

    public Long getTilingType() {
        return getObject().getIntegerKey(ASAtom.TILING_TYPE);
    }

    public double[] getBBox() {
        return TypeConverter.getRealArray(getKey(ASAtom.BBOX), 4, "BBox");
    }

    public Double getXStep() {
        return getObject().getRealKey(ASAtom.X_STEP);
    }

    public Double getYStep() {
        return getObject().getRealKey(ASAtom.Y_STEP);
    }

    public double[] getMatrix() {
        return TypeConverter.getRealArray(getKey(ASAtom.MATRIX), 6, "Matrix");
    }

    public PDResources getResources() {
        COSObject resources = getKey(ASAtom.RESOURCES);
        if (resources != null && resources.getType() == COSObjType.COS_DICT) {
            return new PDResources(resources);
        }
        return null;
    }
}
