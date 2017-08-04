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
package org.verapdf.pd.font.type3;

import org.verapdf.cos.COSObjType;
import org.verapdf.cos.COSObject;
import org.verapdf.pd.PDContentStream;
import org.verapdf.pd.PDObject;

/**
 * Class represents content stream that constructs and paints the glyph for
 * Type 3 font character.
 *
 * @author Sergey Shemyakov
 */
public class PDType3CharProc extends PDObject implements PDContentStream {

    /**
     * Constructor from stream.
     *
     * @param charStream is COSObject containing charProc content stream.
     */
    public PDType3CharProc(COSObject charStream) {
        super(charStream);
    }

    /**
     * @return COSStream, containing charProc content stream.
     */
    @Override
    public COSObject getContents() {
        return getObject();
    }

    /**
     * @param contents is COSStream, containing charProc content stream.
     */
    @Override
    public void setContents(COSObject contents) {
        if (contents != null && contents.getType() == COSObjType.COS_STREAM) {
            setObject(contents);
        }
    }
}
