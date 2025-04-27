/**
 * This file is part of veraPDF Parser, a module of the veraPDF project.
 * Copyright (c) 2015-2025, veraPDF Consortium <info@verapdf.org>
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
package org.verapdf.cos.visitor;

import org.verapdf.cos.*;

/**
 * @author Timur Kamalov
 */
public interface ICOSVisitor {

    Object visitFromBoolean(final COSBoolean obj);
    Object visitFromInteger(final COSInteger obj);
    Object visitFromReal(final COSReal obj);
    Object visitFromString(final COSString obj);
    Object visitFromName(final COSName obj);
    Object visitFromArray(final COSArray obj);
    Object visitFromDictionary(final COSDictionary obj);
    Object visitFromDocument(final COSDocument obj);
    Object visitFromStream(final COSStream obj);
    Object visitFromNull(final COSNull obj);

}
