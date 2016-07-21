package org.verapdf.pd.colors;

import org.apache.log4j.Logger;
import org.verapdf.cos.COSObject;
import org.verapdf.pd.PDObject;

/**
 * @author Maksim Bezrukov
 */
public class PDTristimulus extends PDObject {

    private static final Logger LOGGER = Logger.getLogger(PDTristimulus.class);

    public PDTristimulus(COSObject obj) {
        super(obj);
        if (obj.size() != 3) {
            LOGGER.debug("Tristimulus object doesn't consist of three elements");
        }
    }

    public Double getX() {
        return getNumber(getObject().at(0));
    }

    public Double getY() {
        return getNumber(getObject().at(1));
    }

    public Double getZ() {
        return getNumber(getObject().at(2));
    }

    private static Double getNumber(COSObject object) {
        if (object != null) {
            return object.getReal();
        }
        return null;
    }
}
