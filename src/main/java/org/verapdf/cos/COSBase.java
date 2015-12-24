package org.verapdf.cos;

import org.verapdf.as.ASAtom;

/**
 * @author Timur Kamalov
 */
public abstract class COSBase {

	private int count;

	public COSBase() {
		this.count = 0;
	}

	// Returns object type
	public abstract COSObjType getType();

	// BOOLEAN VALUES
	public abstract boolean getBoolean();
	public abstract boolean setBoolean(final boolean value);

	// INTEGER NUMBERS
	public abstract long getInteger();
	public abstract boolean setInteger(final long value);

	// REAL NUMBERS
	public abstract double getReal();
	public abstract boolean setReal(final double value);

	// STRINGS
	public abstract String getString();
	public abstract boolean setString(final String value);
	public abstract boolean setString(final String value, final boolean isHex);

	// NAMES
	public abstract ASAtom getName();
	public abstract boolean setName(final ASAtom value);

	// NUMBERS OF ELEMENTS FOR ARRAY AND DICTIONARY
	public abstract int size();

	// ARRAYS
	public abstract COSObject at(final int i);
	public abstract boolean add(final COSObject value);
	public abstract boolean set(final int i, final COSObject value);
	public abstract boolean insert(final int i, final COSObject value);
	public abstract void remove(final int i);
	public abstract boolean setArray();
	public abstract boolean setArray(final int size, final COSObject[] value);
	public abstract boolean setArray(final int size, final double[] value);
	public abstract void clearArray();

	// DICTIONARIES
	public abstract boolean knownKey(final ASAtom key);
	public abstract COSObject getKey(final ASAtom key);
	public abstract boolean setKey(final ASAtom key, final COSObject value);
	public abstract boolean getBooleanKey(final ASAtom key);
	public abstract boolean setBooleanKey(final ASAtom key, final boolean value);
	public abstract long getIntegerKey(final ASAtom key);
	public abstract boolean setIntegerKey(final ASAtom key, final long value);
	public abstract double getRealKey(final ASAtom key);
	public abstract boolean setRealKey(final ASAtom key, final double value);
	public abstract String getStringKey(final ASAtom key);
	public abstract boolean setRealKey(final ASAtom key, final String value);
	public abstract ASAtom getNameKey(final ASAtom key);
	public abstract boolean setNameKey(final ASAtom key, final ASAtom value);
	public abstract boolean setArrayKey(final ASAtom key);
	public abstract boolean setArrayKey(final ASAtom key, final int size, final COSObject[] value);
	public abstract boolean setArrayKey(final ASAtom key, final int size, final double[] value);
	public abstract void removeKey(final ASAtom key);

	// STREAMS

	public abstract COSKey getKey();

	public abstract COSObject getObject();

	//TODO : seems this code is related to memory management. Not required in java
	public void acquire() {
		++this.count;
	}

	public void release() {
		if (--this.count == 0) {
			//delete this object
		}
	}

}
