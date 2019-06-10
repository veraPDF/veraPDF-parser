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
package org.verapdf.parser;

import org.verapdf.cos.COSKey;
import org.verapdf.cos.COSTrailer;
import org.verapdf.cos.xref.COSXRefInfo;
import org.verapdf.io.COSXRefTableReader;
import org.verapdf.io.IReader;

import java.util.List;
import java.util.SortedSet;

/**
 * @author Timur Kamalov
 */
public abstract class XRefReader implements IReader {

	private COSXRefTableReader xref;

	//CONSTRUCTORS
	public XRefReader() {
		this.xref = new COSXRefTableReader();
	}

	public XRefReader(final List<COSXRefInfo> infos) {
		this.xref = new COSXRefTableReader(infos);
	}

	public XRefReader(final COSXRefInfo info) {
		this.xref = new COSXRefTableReader(info);
	}

	//PUBLIC METHODS
	public List<COSKey> getKeys() {
		return this.xref.getKeys();
	}

	public long getStartXRef() {
		return this.xref.getStartXRef();
	}

	public SortedSet<Long> getStartXRefs() {
		return this.xref.getStartXRefs();
	}

	public COSTrailer getTrailer() {
		return this.xref.getTrailer();
	}

	public COSTrailer getFirstTrailer() {
		return this.xref.getFirstTrailer();
	}

	public COSTrailer getLastTrailer() {
		return this.xref.getLastTrailer();
	}

	//PROTECTED METHODS
	protected void setXRefInfo(final List<COSXRefInfo> infos) {
		this.xref.set(infos);
	}

	protected void setXRefInfo(final COSXRefInfo info) {
		this.xref.set(info);
	}

	@Override
	public Long getOffset(final COSKey key) {
		return this.xref.getOffset(key);
	}

	protected boolean containsKey(final COSKey key) {
		return this.xref.containsKey(key);
	}

}
