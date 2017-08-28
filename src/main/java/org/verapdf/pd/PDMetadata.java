/**
 * This file is part of veraPDF Parser, a module of the veraPDF project.
 * Copyright (c) 2015, veraPDF Consortium <info@verapdf.org>
 * All rights reserved.
 *
 * veraPDF Parser is free software: you can redistribute it and/or modify
 * it under the terms of either:
 *
 * The GNU General public license GPLv3+.
 * You should have received a copy of the GNU General Public License
 * along with veraPDF Parser as the LICENSE.GPL file in the root of the source
 * tree.  If not, see http://www.gnu.org/licenses/ or
 * https://www.gnu.org/licenses/gpl-3.0.en.html.
 *
 * The Mozilla Public License MPLv2+.
 * You should have received a copy of the Mozilla Public License along with
 * veraPDF Parser as the LICENSE.MPL file in the root of the source tree.
 * If a copy of the MPL was not distributed with this file, you can obtain one at
 * http://mozilla.org/MPL/2.0/.
 */
package org.verapdf.pd;

import org.verapdf.as.ASAtom;
import org.verapdf.cos.COSBase;
import org.verapdf.cos.COSObjType;
import org.verapdf.cos.COSObject;
import org.verapdf.cos.COSStream;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

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
                            LOGGER.log(Level.SEVERE, "Filter array contain non COSName element");
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
		LOGGER.log(Level.SEVERE, "Metadata object is not a stream");
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
