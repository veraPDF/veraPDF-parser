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
package org.verapdf.pd.function;

import org.verapdf.as.ASAtom;
import org.verapdf.cos.COSObjType;
import org.verapdf.cos.COSObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PDType3Function extends PDFunction {

	private static final Logger LOGGER = Logger.getLogger(PDType3Function.class.getCanonicalName());

	protected PDType3Function(COSObject obj) {
		super(obj);
	}

	public List<PDFunction> getFunctions() {
		COSObject obj = getKey(ASAtom.FUNCTIONS);
		if (obj.getType() != COSObjType.COS_ARRAY) {
			LOGGER.log(Level.WARNING, "Invalid Functions key value in Type 3 Function dictionary");
			return Collections.emptyList();
		}

		List<PDFunction> pdFunctions = new ArrayList<>();
		for (int i = 0; i < obj.size(); i++) {
			PDFunction function = PDFunction.createFunction(obj.at(i));
			if (function != null) {
				pdFunctions.add(function);
			}
		}
		return Collections.unmodifiableList(pdFunctions);
	}
}
