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

	private COSTrailer firstTrailer;
	private COSTrailer lastTrailer;

	public COSXRefTableReader() {
		this.startXRef = 0;
		this.offsets = new HashMap<>();
		this.trailer = new COSTrailer();
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

		setFirstAndLastTrailers(trailers);

		infos.clear();
	}

	public void setFirstAndLastTrailers(Map<Long, COSTrailer> trailers) {
		if (trailers.isEmpty()) {
			return;
		}
		Set<Long> offsets = trailers.keySet();
		SortedSet<Long> sortedOffset = new TreeSet<>(offsets);
		this.firstTrailer = trailers.get(sortedOffset.first());
		this.lastTrailer = trailers.get(sortedOffset.last());
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
		List<COSKey> result = new ArrayList<COSKey>();
		for (Map.Entry<COSKey, Long> entry : this.offsets.entrySet()) {
			result.add(entry.getKey());
		}
		return result;
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

}
