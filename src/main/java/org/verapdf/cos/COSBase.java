package org.verapdf.cos;

import org.verapdf.as.ASAtom;
import org.verapdf.as.io.ASInputStream;
import org.verapdf.cos.visitor.ICOSVisitor;
import org.verapdf.cos.visitor.IVisitor;

import java.util.Collection;
import java.util.Set;

/**
 * @author Timur Kamalov
 */
public abstract class COSBase {

	public COSBase() {
	}

	// Returns object type
	public abstract COSObjType getType();

	// VISITOR DESIGN PATTERN
	public abstract void accept(final IVisitor visitor);
	public abstract Object accept(final ICOSVisitor visitor);

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
	public abstract boolean setStringKey(final ASAtom key, final String value);
	public abstract ASAtom getNameKey(final ASAtom key);
	public abstract boolean setNameKey(final ASAtom key, final ASAtom value);
	public abstract boolean setArrayKey(final ASAtom key);
	public abstract boolean setArrayKey(final ASAtom key, final int size, final COSObject[] value);
	public abstract boolean setArrayKey(final ASAtom key, final int size, final double[] value);
	public abstract void removeKey(final ASAtom key);
	public abstract Set<ASAtom> getKeySet();
	public abstract Collection<COSObject> getValues();

	// STREAMS
	public abstract ASInputStream getData();
	public abstract ASInputStream getData(final COSStream.FilterFlags flags);

	public abstract boolean setData(final ASInputStream stream);
	public abstract boolean setData(final ASInputStream stream, final COSStream.FilterFlags flags);

	// INDIRECT OBJECT
	public abstract boolean isIndirect();
	public abstract COSKey getKey();
	public abstract COSDocument getDocument();
	public abstract boolean setKey(final COSKey key, final COSDocument document);
	public abstract COSObject getDirect();
	public abstract boolean setDirect(final COSObject value);

	public abstract void mark();

}
