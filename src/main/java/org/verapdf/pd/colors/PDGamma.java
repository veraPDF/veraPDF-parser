package org.verapdf.pd.colors;

import org.apache.log4j.Logger;
import org.verapdf.cos.COSObject;
import org.verapdf.pd.PDObject;

/**
 * @author Maksim Bezrukov
 */
public class PDGamma extends PDObject {

    private static final Logger LOGGER = Logger.getLogger(PDGamma.class);

    public PDGamma(COSObject obj) {
        super(obj);
        if (obj.size() != 3) {
            LOGGER.debug("Gamma object doesn't consist of three elements");
        }
    }

    public Double getR() {
        return getNumber(getObject().at(0));
    }

    public Double getG() {
        return getNumber(getObject().at(1));
    }

    public Double getB() {
        return getNumber(getObject().at(2));
    }

    private static Double getNumber(COSObject object) {
        if (object != null) {
            return object.getReal();
        }
        return null;
    }
}
