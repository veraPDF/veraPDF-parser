package org.verapdf.cos;

/**
 * @author Timur Kamalov
 */
public class COSKey {

	private int number;
	private int generation;

	public COSKey() {
		this(0);
	}

	public COSKey(int number) {
		this(number, 0);
	}

	public COSKey(int number, int generation) {
		this.number = number;
		this.generation = generation;
	}

	public int getNumber() {
		return number;
	}

	public void setNumber(int number) {
		this.number = number;
	}

	public int getGeneration() {
		return generation;
	}

	public void setGeneration(int generation) {
		this.generation = generation;
	}

	@Override
	public String toString() {
		return number + " " + generation + " obj";
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		COSKey cosKey = (COSKey) o;

		if (number != cosKey.number) return false;
		return generation == cosKey.generation;

	}

	@Override
	public int hashCode() {
		int result = number;
		result = 31 * result + generation;
		return result;
	}

}