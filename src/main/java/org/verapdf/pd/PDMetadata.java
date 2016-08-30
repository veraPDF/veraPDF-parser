package org.verapdf.pd;

import org.apache.log4j.Logger;
import org.verapdf.as.ASAtom;
import org.verapdf.cos.COSBase;
import org.verapdf.cos.COSObjType;
import org.verapdf.cos.COSObject;
import org.verapdf.cos.COSStream;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Maksim Bezrukov
 */
public class PDMetadata extends PDObject {

    private static final Logger LOGGER = Logger.getLogger(PDMetadata.class);

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
                    for (int i = 0; i < filters.size(); ++i) {
                        COSObject elem = filters.at(i);
                        if (elem.getType() == COSObjType.COS_NAME) {
                            res.add(elem.getName());
                        } else {
                            LOGGER.debug("Filter array contain non COSName element");
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
        } else {
            LOGGER.debug("Current object is not a stream");
            return null;
        }
    }

    public InputStream getStream() {
        COSStream stream = getCOSStream();
        if (stream != null) {
            return stream.getData(COSStream.FilterFlags.DECODE);
        }
        return null;
    }
}
