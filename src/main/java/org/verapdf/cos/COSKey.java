package org.verapdf.cos;

/**
 * @author Timur Kamalov
 */
public class COSKey {

	private long number;
	private long generation;

	public COSKey() {
		this(0);
	}

	public COSKey(long number) {
		this(number, 0);
	}

	public COSKey(long number, long generation) {
		this.number = number;
		this.generation = generation;
	}



	public long getNumber() {
		return number;
	}

	public void setNumber(long number) {
		this.number = number;
	}

	public long getGeneration() {
		return generation;
	}

	public void setGeneration(long generation) {
		this.generation = generation;
	}

}