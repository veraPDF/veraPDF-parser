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
package org.verapdf.cos.xref;

import org.verapdf.cos.COSObject;
import org.verapdf.cos.COSTrailer;

/**
 * Class represents xref section with trailer.
 *
 * @author Timur Kamalov
 */
public class COSXRefInfo {

	private long startXRef;
	private COSXRefSection xref;
	private final COSTrailer trailer;

	/**
	 * Creates empty COSXrefInfo object.
	 */
	public COSXRefInfo() {
		this.startXRef = 0;
		this.xref = new COSXRefSection();
		this.trailer = new COSTrailer();
	}

	/**
	 * @return offset of xref section.
	 */
	public long getStartXRef() {
		return this.startXRef;
	}

	/**
	 * Sets offset of xref section.
	 */
	public void setStartXRef(final long startXRef) {
		this.startXRef = startXRef;
	}

	/**
	 * @return xref section object.
	 */
	public COSXRefSection getXRefSection() {
		return this.xref;
	}

	/**
	 * Sets xref section object.
	 */
	public void setXref(COSXRefSection xref) {
		this.xref = xref;
	}

	/**
	 * @return trailer of this section.
	 */
	public COSTrailer getTrailer() {
		return this.trailer;
	}

	/**
	 * Sets trailer of this section.
	 */
	public void setTrailer(final COSObject object) {
		this.trailer.setObject(object);
	}

}
