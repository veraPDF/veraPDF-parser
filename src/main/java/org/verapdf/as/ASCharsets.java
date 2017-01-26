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
package org.verapdf.as;

import java.nio.charset.Charset;

/**
 * @author Timur Kamalov
 */
public final class ASCharsets {

	/*** ASCII charset */
	public static final Charset US_ASCII = Charset.forName("US-ASCII");

	/*** UTF-16BE charset */
	public static final Charset UTF_16BE = Charset.forName("UTF-16BE");

	/*** UTF-16LE charset */
	public static final Charset UTF_16LE = Charset.forName("UTF-16LE");

	/*** ISO-8859-1 charset */
	public static final Charset ISO_8859_1 = Charset.forName("ISO-8859-1");

	/*** UTF-8 charset */
	public static final Charset UTF_8 = Charset.forName("UTF-8");

}
