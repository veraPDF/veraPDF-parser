package org.verapdf.pd;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.verapdf.as.ASAtom;
import org.verapdf.cos.COSBase;
import org.verapdf.cos.COSObjType;
import org.verapdf.cos.COSObject;
import org.verapdf.cos.COSStream;

/**
 * @author Maksim Bezrukov
 */
public class PDMetadata extends PDObject {

    private static final Logger LOGGER = Logger.getLogger(PDMetadata.class.getCanonicalName());

    public PDMetadata(COSObject obj) {
        super(obj);
    }

    public List<ASAtom> getFilters() {
        COSObject filters = getKey(ASAtom.FILTER);
        if (filters != null) {
            List<ASAtom> res = new ArrayList<>();
            switch (filters.getType()) {
                case COS_NAME:
                    res.add(filters.getName());
                    break;
                case COS_ARRAY:
                    for (int i = 0; i < filters.size().intValue(); ++i) {
                        COSObject elem = filters.at(i);
                        if (elem.getType() == COSObjType.COS_NAME) {
                            res.add(elem.getName());
                        } else {
                            LOGGER.log(Level.FINE, "Filter array contain non COSName element");
                        }
                    }
                    break;
            }
            return Collections.unmodifiableList(res);
        }
        return Collections.emptyList();
    }

    public COSStream getCOSStream() {
        COSBase currentObject = getObject().getDirectBase();
        if (currentObject.getType() == COSObjType.COS_STREAM) {
            return (COSStream) currentObject;
        }
		LOGGER.log(Level.FINE, "Current object is not a stream");
		return null;
    }

    public InputStream getStream() {
        COSStream stream = getCOSStream();
        if (stream != null) {
            return stream.getData(COSStream.FilterFlags.DECODE);
        }
        return null;
    }
}
