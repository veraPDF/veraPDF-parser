package org.verapdf.cos.xref;

/**
 * @author Timur Kamalov
 */
public class COSXRefEntry {

	public long offset;
	public int generation;
	public char free;

	public COSXRefEntry() {
		this(0, 0, 'n');
	}

	public COSXRefEntry(long offset, int generation) {
		this(offset, generation, 'n');
	}

	public COSXRefEntry(long offset, int generation, char free) {
		this.offset = offset;
		this.generation = generation;
		this.free = free;
	}
}
