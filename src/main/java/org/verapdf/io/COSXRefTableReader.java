package org.verapdf.io;

import org.verapdf.cos.COSKey;
import org.verapdf.cos.COSTrailer;
import org.verapdf.cos.COSXRefInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Timur Kamalov
 */
public class COSXRefTableReader {

	private long startXRef;
	private Map<COSKey, Long> offsets;
	private COSTrailer trailer;


	public COSXRefTableReader() {
	}

	public COSXRefTableReader(final List<COSXRefInfo> info) {
	}

	public COSXRefTableReader(final COSXRefInfo info) {
	}

	public void set(final List<COSXRefInfo> infos) {
		this.startXRef = 0;
		this.offsets.clear();
		this.trailer.clear();

		if (infos == null && infos.isEmpty()) {
			return;
		}

		COSXRefInfo lastInfo = infos.get(infos.size()-1);
		this.startXRef = lastInfo.getStartXRef();
		this.trailer = lastInfo.getTrailer();

		for (COSXRefInfo info : infos) {
			info.getXRefSection().addTo(this.offsets);
		}

		infos.clear();
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
		if (this.offsets.containsKey(key)) {
			return this.offsets.get(key);
		} else {
			return 0;
		}
	}

	public COSTrailer getTrailer() {
		return this.trailer;
	}

}
