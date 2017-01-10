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
package org.verapdf.as.exceptions;

/**
 * @author Timur Kamalov
 */
public class StringExceptions {

	public static final String UNKNOWN_TYPE_PAGE_TREE_NODE = "unknown type of page tree node";

	//PDFAPI
	public static final String CAN_NOT_LOCATE_XREF_TABLE = "can not locate xref table";
	public static final String START_XREF_VALIDATION = "startxref validation failed";
	public static final String ENCRYPTED_PDF_NOT_SUPPORTED = "encrypted pdf is not supported";
	public static final String XREF_STM_NOT_SUPPORTED = "xref streams not supported";
	public static final String INVALID_PDF_OBJECT = "invalid pdf object";
	public static final String INVALID_PDF_ARRAY = "invalid pdf array";
	public static final String INVALID_PDF_DICTONARY = "invalid pdf dictonary";
	public static final String INVALID_PDF_STREAM = "invalid pdf stream";

	public static final String DUPLICATE_FACTORY_NAMES = "internal library error";

	public static final String WRITE_ERROR = "Error writing document";

}
