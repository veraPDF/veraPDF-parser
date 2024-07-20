/**
 * This file is part of veraPDF Parser, a module of the veraPDF project.
 * Copyright (c) 2015-2024, veraPDF Consortium <info@verapdf.org>
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
package org.verapdf.operator;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Timur Kamalov
 */
public class Operator {

	private static final Map<String, Operator> CACHED_OPERATORS = new HashMap<>();

	private final String operator;

	protected Operator(final String operator) {
		this.operator = operator;
	}

	public static Operator getOperator(final String operator) {
		//don't cache image operators due to unique parameters and data
		if ("BI".equals(operator) || "ID".equals(operator)) {
			return new InlineImageOperator(operator);
		} else {
			if (CACHED_OPERATORS.containsKey(operator)) {
				return CACHED_OPERATORS.get(operator);
			} else {
				Operator result = new Operator(operator);
				CACHED_OPERATORS.put(operator, result);
				return result;
			}
		}
	}

	public String getOperator() {
		return operator;
	}

	@Override
	public String toString() {
		return "PDFOperator{" + operator + '}';
	}

}
