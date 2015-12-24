package org.verapdf.cos;

import org.verapdf.as.ASAtom;

/**
 * Created by Timur on 12/17/2015.
 */
public class COSDirect extends COSBase {

    // OBJECT TYPE
    public COSObjType getType() {
        return COSObjType.COSUndefinedT;
    }

    // BOOLEAN VALUES
    public boolean getBoolean() {
        return false;
    }

    public boolean setBoolean(final boolean value) {
        return false;
    }

    // INTEGER NUMBERS
    public long getInteger() {
        return 0;
    }

    public boolean setInteger(final long value) {
        return false;
    }

    // REAL NUMBERS
    public double getReal() {
        return 0;
    }

    public boolean setReal(final double value) {
        return false;
    }

    // STRINGS
    public String getString() {
        return "";
    }

    public boolean setString(final String value) {
        return setString(value, false);
    }

    public boolean setString(final String value, final boolean isHex) {
        return false;
    }

    // NAMES
    public ASAtom getName() {
        final ASAtom empty = new ASAtom();
        return empty;
    }

    public boolean setName(final ASAtom value) {
        return false;
    }

    // NUMBERS OF ELEMENTS FOR ARRAY AND DICTIONARY
    public int size() {
        return 1;
    }

    // ARRAYS
    public COSObject at(final int i) {
        return COSObject.getEmpty();
    }

    public COSObject at(final int i) {
        return new COSObject(this);
    }

    public boolean add(final COSObject value) {
        return false;
    }

    public boolean set(final int i, final COSObject value) {
        return false;
    }

    public boolean insert(final int i, final COSObject value) {
        return false;
    }

    public void remove(final int i);
    public boolean setArray();
    public boolean setArray(final int size, final COSObject[] value);
    public boolean setArray(final int size, final double[] value);
    public void clearArray();

    // DICTIONARIES
    public boolean knownKey(final ASAtom key);
    public COSObject getKey(final ASAtom key);
    public boolean setKey(final ASAtom key, final COSObject value);
    public boolean getBooleanKey(final ASAtom key);
    public boolean setBooleanKey(final ASAtom key, final boolean value);
    public long getIntegerKey(final ASAtom key);
    public boolean setIntegerKey(final ASAtom key, final long value);
    public double getRealKey(final ASAtom key);
    public boolean setRealKey(final ASAtom key, final double value);
    public String getStringKey(final ASAtom key);
    public boolean setRealKey(final ASAtom key, final String value);
    public ASAtom getNameKey(final ASAtom key);
    public boolean setNameKey(final ASAtom key, final ASAtom value);
    public boolean setArrayKey(final ASAtom key);
    public boolean setArrayKey(final ASAtom key, final int size, final COSObject[] value);
    public boolean setArrayKey(final ASAtom key, final int size, final double[] value);
    public void removeKey(final ASAtom key);

}
