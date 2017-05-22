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
package org.verapdf.as.filters;

import org.verapdf.as.io.ASInputStream;
import org.verapdf.as.io.ASOutputStream;
import org.verapdf.cos.COSDictionary;

import java.io.IOException;

/**
 * Interface for filter factories.
 *
 * @author Timur Kamalov
 */
public interface IASFilterFactory {

	/**
	 * Gets input filter from given encoded stream with specified decode
	 * parameters.
	 *
	 * @param inputStream is encoded data.
	 * @param decodeParams is dictionary with parameters for filter.
	 * @return stream with decoded data.
	 */
	ASInFilter getInFilter(ASInputStream inputStream, COSDictionary decodeParams) throws IOException;

	/**
	 * Gets output filter with encoded data.
	 *
	 * @param outputStream stream to encode.
	 * @return stream with encoded data.
	 */
	ASOutFilter getOutFilter(ASOutputStream outputStream) throws IOException;

}
