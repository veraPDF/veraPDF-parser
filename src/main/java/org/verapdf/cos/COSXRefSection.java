package org.verapdf.cos;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Timur Kamalov
 */
public class COSXRefSection {

	private Map<Long, COSXRefEntry> entries;

	public COSXRefSection() {
		this.entries = new HashMap<Long, COSXRefEntry>();
		this.entries.put(0L, new COSXRefEntry(0, 65535, 'f'));
	}

	public void add(final COSKey key, final long offset) {
		this.add(key, offset, 'n');
	}

	public void add(final COSKey key, final long offset, final char free) {
		this.entries.put(key.getNumber(), new COSXRefEntry(offset, key.getGeneration(), free));
	}

	public void add(final Map<COSKey, Long> offsets) {
		this.add(offsets, 'n');
	}

	public void add(final Map<COSKey, Long> offsets, final char free) {
		for (Map.Entry<COSKey, Long> entry : offsets.entrySet()) {
			this.add(entry.getKey(), entry.getValue(), free);
		}
	}

	public void addTo(final List<COSKey> keys) {
		for (Map.Entry<Long, COSXRefEntry> entry : this.entries.entrySet()) {
			final COSKey key = new COSKey(entry.getKey(), entry.getValue().generation);
			if (entry.getValue().free == 'n') {
				keys.add(key);
			} else {
				removeIfNumberEqual(keys, key.getNumber());
			}
		}
		//TODO : sort?
		//TODO : unique?
	}

	public void addTo(final Map<COSKey, Long> offsets) {
		for (Map.Entry<Long, COSXRefEntry> entry : this.entries.entrySet()) {
			final COSKey key = new COSKey(entry.getKey(), entry.getValue().generation);
			if (entry.getValue().free == 'n') {
				offsets.put(key, entry.getValue().offset);
			} else {
				offsets.remove(new COSKey(key.getNumber(), key.getGeneration() - 1));
			}
		}
	}

	public COSXRefEntry getEntry(final long number) {
		return this.entries.get(number);
	}

	public void addEntry(final long number, final COSXRefEntry entry) {
		this.entries.put(number, entry);
	}

	private void removeIfNumberEqual(final List<COSKey> keys, final long number) {
		for (COSKey key : keys) {
			if (key.getNumber() == number) {
				keys.remove(number);
			}
		}
	}

}
