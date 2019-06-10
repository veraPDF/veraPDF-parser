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
package org.verapdf.io;

import org.verapdf.cos.COSKey;
import org.verapdf.cos.COSTrailer;
import org.verapdf.cos.xref.COSXRefInfo;

import java.util.*;

/**
 * @author Timur Kamalov
 */
public class COSXRefTableReader {

	private long startXRef;
	private Map<COSKey, Long> offsets;
	private COSTrailer trailer;

	private SortedSet<Long> startXRefs;

	private COSTrailer firstTrailer;
	private COSTrailer lastTrailer;

	public COSXRefTableReader() {
		this.startXRef = 0;
		this.offsets = new HashMap<>();
		this.trailer = new COSTrailer();
		this.startXRefs = new TreeSet<>();
	}

	public COSXRefTableReader(final List<COSXRefInfo> info) {
		this();
		set(info);
	}

	public COSXRefTableReader(final COSXRefInfo info) {
		this();
		set(info);
	}

	public void set(final List<COSXRefInfo> infos) {
		this.startXRef = 0;
		this.offsets.clear();
		this.trailer.clear();
		this.startXRefs.clear();

		if (infos == null || infos.isEmpty()) {
			return;
			}

		COSXRefInfo lastInfo = infos.get(infos.size()-1);
		this.startXRef = lastInfo.getStartXRef();
		this.trailer = lastInfo.getTrailer();

		Map<Long, COSTrailer> trailers = new HashMap<>();
		for (COSXRefInfo info : infos) {
			trailers.put(info.getStartXRef(), info.getTrailer());
			info.getXRefSection().addTo(this.offsets);
		}

		setFirstLastTrailersAndStartXRefs(trailers);

		infos.clear();
	}

	public void setFirstLastTrailersAndStartXRefs(Map<Long, COSTrailer> trailers) {
		if (trailers.isEmpty()) {
			return;
		}

		Set<Long> offsets = trailers.keySet();
		this.startXRefs.addAll(offsets);
		this.firstTrailer = trailers.get(this.startXRefs.first());
		this.lastTrailer = trailers.get(this.startXRefs.last());
	}

	public void set(final COSXRefInfo info) {
		this.startXRef = info.getStartXRef();

		this.offsets.clear();
		info.getXRefSection().addTo(this.offsets);

		this.trailer = info.getTrailer();
	}

	public long getStartXRef() {
		return this.startXRef;
	}

	public List<COSKey> getKeys() {
		return new ArrayList<>(offsets.keySet());
	}

	public long getOffset(final COSKey key) {
		Long value = this.offsets.get(key);
		return value != null ? value : 0;
	}

	public boolean containsKey(final COSKey key) {
		return this.offsets.containsKey(key);
	}

	public COSTrailer getTrailer() {
		return this.trailer;
	}

	public COSTrailer getFirstTrailer() {
		return this.firstTrailer;
	}

	public COSTrailer getLastTrailer() {
		return this.lastTrailer;
	}

	public SortedSet<Long> getStartXRefs() {
		return Collections.unmodifiableSortedSet(this.startXRefs);
	}
}
