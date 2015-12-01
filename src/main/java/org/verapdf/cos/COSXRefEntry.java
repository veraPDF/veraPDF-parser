package org.verapdf.cos;

/**
 * @author Timur Kamalov
 */
public class COSXRefEntry {

	public long offset;
	public long generation;
	public char free;

	public COSXRefEntry() {
		this(0, 0, 'n');
	}

	public COSXRefEntry(long offset, long generation) {
		this(offset, generation, 'n');
	}

	public COSXRefEntry(long offset, long generation, char free) {
		this.offset = offset;
		this.generation = generation;
		this.free = free;
	}
}
