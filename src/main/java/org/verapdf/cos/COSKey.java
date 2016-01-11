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

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		COSKey cosKey = (COSKey) o;

		if (number != cosKey.number) return false;
		return generation == cosKey.generation;

	}

}