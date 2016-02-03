package org.verapdf.cos.xref;

import org.verapdf.cos.COSKey;

import java.util.*;

/**
 * @author Timur Kamalov
 */
public class COSXRefSection {

	private Map<Integer, COSXRefEntry> entries;

	public COSXRefSection() {
		this.entries = new HashMap<Integer, COSXRefEntry>();
		this.entries.put(0, new COSXRefEntry(0, 65535, 'f'));
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
		for (Map.Entry<Integer, COSXRefEntry> entry : this.entries.entrySet()) {
			final COSKey key = new COSKey(entry.getKey(), entry.getValue().generation);
			if (entry.getValue().free == 'n') {
				keys.add(key);
			} else {
				removeIfNumberEqual(keys, key.getNumber());
			}
		}
	}

	public void addTo(final Map<COSKey, Long> offsets) {
		for (Map.Entry<Integer, COSXRefEntry> entry : this.entries.entrySet()) {
			final COSKey key = new COSKey(entry.getKey(), entry.getValue().generation);
			if (entry.getValue().free == 'n') {
				offsets.put(key, entry.getValue().offset);
			} else {
				offsets.remove(new COSKey(key.getNumber(), key.getGeneration() - 1));
			}
		}
	}

	public List<COSXRefRange> getRange() {
		List<COSXRefRange> result = new ArrayList<COSXRefRange>();

		if (this.entries.isEmpty()) {
			return result;
		}

		Iterator<Map.Entry<Integer, COSXRefEntry>> iterator = this.entries.entrySet().iterator();
		COSXRefRange segment = new COSXRefRange(iterator.next().getKey());
		int nextSegment;
		while(iterator.hasNext()) {
			nextSegment = iterator.next().getKey();
			if (nextSegment == segment.next()) {
				segment.count++;
				continue;
			} else {
				result.add(segment);
				segment = new COSXRefRange(nextSegment);
			}
		}
		result.add(segment);

		return result;
	}

	public COSXRefEntry getEntry(final int number) {
		return this.entries.get(number);
	}

	public void addEntry(final int number, final COSXRefEntry entry) {
		this.entries.put(number, entry);
	}

	private void removeIfNumberEqual(final List<COSKey> keys, final int number) {
		for (COSKey key : keys) {
			if (key.getNumber() == number) {
				keys.remove(number);
			}
		}
	}

	public long next() {
		if (entries.isEmpty()) {
			return 1;
		} else {
			// TODO : no nice way to get last element of linked hash map
			Iterator<Map.Entry<Integer, COSXRefEntry>> iterator = this.entries.entrySet().iterator();
			Map.Entry<Integer, COSXRefEntry> lastElement = iterator.next();
			while (iterator.hasNext()) {
				lastElement = iterator.next();
			}
			return lastElement.getKey() + 1;
		}
	}

}
