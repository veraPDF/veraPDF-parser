package org.verapdf.cos;

import org.verapdf.as.ASAtom;

import java.io.IOException;

/**
 * @author Timur Kamalov
 */
public abstract class COSBase {

	public COSBase() {
	}

	// Returns object type
	public abstract COSObjType getType() throws IOException;

	// BOOLEAN VALUES
	public abstract boolean getBoolean() throws IOException;
	public abstract boolean setBoolean(final boolean value) throws IOException;

	// INTEGER NUMBERS
	public abstract long getInteger() throws IOException;
	public abstract boolean setInteger(final long value) throws IOException;

	// REAL NUMBERS
	public abstract double getReal() throws IOException;
	public abstract boolean setReal(final double value) throws IOException;

	// STRINGS
	public abstract String getString() throws IOException;
	public abstract boolean setString(final String value) throws IOException;
	public abstract boolean setString(final String value, final boolean isHex) throws IOException;

	// NAMES
	public abstract ASAtom getName() throws IOException;
	public abstract boolean setName(final ASAtom value) throws IOException;

	// NUMBERS OF ELEMENTS FOR ARRAY AND DICTIONARY
	public abstract int size() throws IOException;

	// ARRAYS
	public abstract COSObject at(final int i) throws IOException;
	public abstract boolean add(final COSObject value) throws IOException;
	public abstract boolean set(final int i, final COSObject value) throws IOException;
	public abstract boolean insert(final int i, final COSObject value) throws IOException;
	public abstract void remove(final int i) throws IOException;
	public abstract boolean setArray() throws IOException;
	public abstract boolean setArray(final int size, final COSObject[] value) throws IOException;
	public abstract boolean setArray(final int size, final double[] value) throws IOException;
	public abstract void clearArray() throws IOException;

	// DICTIONARIES
	public abstract boolean knownKey(final ASAtom key) throws IOException;
	public abstract COSObject getKey(final ASAtom key) throws IOException;
	public abstract boolean setKey(final ASAtom key, final COSObject value) throws IOException;
	public abstract boolean getBooleanKey(final ASAtom key) throws IOException;
	public abstract boolean setBooleanKey(final ASAtom key, final boolean value) throws IOException;
	public abstract long getIntegerKey(final ASAtom key) throws IOException;
	public abstract boolean setIntegerKey(final ASAtom key, final long value) throws IOException;
	public abstract double getRealKey(final ASAtom key) throws IOException;
	public abstract boolean setRealKey(final ASAtom key, final double value) throws IOException;
	public abstract String getStringKey(final ASAtom key) throws IOException;
	public abstract boolean setStringKey(final ASAtom key, final String value) throws IOException;
	public abstract ASAtom getNameKey(final ASAtom key) throws IOException;
	public abstract boolean setNameKey(final ASAtom key, final ASAtom value) throws IOException;
	public abstract boolean setArrayKey(final ASAtom key) throws IOException;
	public abstract boolean setArrayKey(final ASAtom key, final int size, final COSObject[] value) throws IOException;
	public abstract boolean setArrayKey(final ASAtom key, final int size, final double[] value) throws IOException;
	public abstract void removeKey(final ASAtom key) throws IOException;

	// INDIRECT OBJECT
	public abstract boolean isIndirect();
	public abstract COSKey getKey();
	public abstract COSDocument getDocument();
	public abstract boolean setKey(final COSKey key, final COSDocument document);
	public abstract COSObject getDirect() throws IOException;
	public abstract boolean setDirect(final COSObject value);

	public abstract void mark() throws IOException;

}
