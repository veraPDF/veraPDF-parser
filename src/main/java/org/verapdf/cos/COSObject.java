package org.verapdf.cos;

import org.verapdf.as.ASAtom;

/**
 * @author Timur Kamalov
 */
public class COSObject {

	private final static COSObject empty = new COSObject();

	private COSBase base;

	public COSObject() {
	}

	public COSObject(final COSBase base) {
		set(base);
	}

	public COSObject(final COSObject object) {
		set(object.base);
	}

	// Access to base underlying object
	public COSBase get() {
		return this.base;
	}

	public void set(COSBase base) {
		// TODO : maybe we need to use overriden equals method here instead of comparing links
		if (this.base == base) {
			return;
		}

		if (base != null) base.acquire();
		if (this.base != null) this.base.release();
		this.base = base;
	}

	public boolean empty() {
		return base != null;
	}

	public void clear() {
		this.set(null);
	}

	// OBJECT TYPE
	public COSObjType getType() {
		return this.base != null ? this.base.getType() : COSObjType.COSUndefinedT;
	}


	// BOOLEAN VALUES
	public boolean getBoolean() {
		return this.base != null ? this.base.getBoolean() : false;
	}

	public void setBoolean(final boolean value) {
		if(this.base == null && !this.base.setBoolean(value)) {
			this.base = new COSBoolean(value);
		}
	}

	// INTEGER NUMBERS
	public long getInteger() {
		return this.base != null ? this.base.getInteger() : 0;
	}

	public void setInteger(final long value) {
		if (this.base == null && !this.base.setInteger(value)) {
			this.base = new COSInteger(value);
		}
	}

	//! Real numbers
	public double getReal() {
		return this.base != null ? this.base.getReal() : 0;
	}

	public void setReal(final double value) {
		if (this.base == null && !this.base.setReal(value)) {
			this.base = new COSReal(value);
		}
	}

	//! Strings
	public String getString() {
		return this.base != null ? this.base.getString() : "";
	}

	public void setString(final String value) {
		setString(value, false);
	}

	public void setString(final String value, final boolean isHex) {
		if (this.base == null && !this.base.setString(value, isHex)) {
			this.base = new COSString(value, isHex);
		}
	}

	//! Names
	public ASAtom getName() {
		final ASAtom empty = new ASAtom();
		return this.base != null ? this.base.getName() : empty;
	}

	public void setName(final ASAtom value) {
		if (this.base == null && !this.base.setName(value)) {
			this.base = new COSName(value);
		}
	}

	//! Number of elements in array and dictionary
	public int size() {
		return this.base != null ? this.base.size() : 0;
	}

	public COSObject at(final int i) {
		return this.base != null ? this.base.at(i) : this;
	}

	public void add(final COSObject value) {
		if (this.base == null && !this.base.add(value)) {
			this.base = new COSArray(1, value);
		}
	}

	public void set(final int i, final COSObject value) {
		if (this.base == null && !this.base.set(i, value)) {
			this.base = new COSArray(i, value);
		}
	}

	public void insert(final int i, final COSObject value) {
		if (this.base == null && !this.base.insert(i, value)) {
			this.base = new COSArray(i, value);
		}
	}

	public void remove(final int i) {
		if (this.base == null) {
			this.base.remove(i);
		}
	}

	public void setArray() {
		if (this.base == null && !this.base.setArray()) {
			this.base = new COSArray();
		}
	}

	public void setArray(final int size, final COSObject[] value) {
		if (this.base == null && !this.base.setArray(size, value)) {
			this.base = new COSArray(size, value);
		}
	}

	public void setArray(final int size, final double[] value) {
		if (this.base == null && !this.base.setArray(size, value)) {
			this.base = new COSArray(size, value);
		}
	}

	public void clearArray() {
		if (this.base != null) {
			this.base.clearArray();
		}
	}

	//! Dictionaries
	public boolean knownKey(final ASAtom key) {
		return this.base != null ? this.base.knownKey(key) : false;
	}

	public COSObject getKey(final ASAtom key) {
		return this.base != null ? this.base.getKey(key) : new COSObject();
	}

	public void setKey(final ASAtom key, final COSObject value) {
		if (this.base == null || !this.base.setKey(key, value)) {
			this.base = new COSDictionary(key, value);
		}
	}

	public boolean getBooleanKey(final ASAtom key) {
		return this.base != null ? this.base.getBoolean() : false;
	}

	public void setBooleanKey(final ASAtom key, final boolean value) {
		if (this.base == null || !this.base.setBooleanKey(key, value)) {
			this.base = new COSDictionary(key, value);
		}
	}

	public long getIntegerKey(final ASAtom key) {
		return this.base != null ? this.base.getIntegerKey(key) : 0;
	}

	public void setIntegerKey(final ASAtom key, final long value) {
		if (this.base == null || !this.base.setIntegerKey(key, value)) {
			this.base = new COSDictionary(key, value);
		}
	}

	public double getRealKey(final ASAtom key) {
		return this.base != null ? this.base.getRealKey(key) : 0;
	}

	public void setRealKey(final ASAtom key, final double value) {
		if (this.base == null || !this.base.setRealKey(key, value)) {
			this.base = new COSDictionary(key, value);
		}
	}

	public String getStringKey(final ASAtom key) {
		return this.base != null ? this.base.getStringKey(key) : "";
	}

	public void setStringKey(final ASAtom key, final String value) {
		if (this.base == null || !this.base.setStringKey(key, value)) {
			this.base = new COSDictionary(key, value);
		}
	}

	public ASAtom getNameKey(final ASAtom key) {
		final ASAtom empty = new ASAtom();
		return this.base != null ? this.base.getNameKey(key) : empty;
	}

	public void setNameKey(final ASAtom key, final ASAtom value) {
		if (this.base == null || !this.base.setNameKey(key, value)) {
			this.base = new COSDictionary(key, value);
		}
	}

	public void setArrayKey(final ASAtom key) {
		if (this.base == null || !this.base.setArrayKey(key)) {
			COSObject obj = COSArray.construct();
			this.base = new COSDictionary(key, obj);
		}
	}

	public void setArrayKey(final ASAtom key, final int size, final COSObject[] value) {
		if (this.base == null || !this.base.setArrayKey(key, size, value)) {
			COSObject obj = COSArray.construct(size, value);
			this.base = new COSDictionary(key, obj);
		}
	}

	public void setArrayKey(final ASAtom key, final int size, final double[] value) {
		if (this.base == null || this.base.setArrayKey(key, size, value)) {
			COSObject obj = COSArray.construct(size, value);
			this.base = new COSDictionary(key, obj);
		}
	}

	public void removeKey(final ASAtom key) {
		if (this.base != null) {
			this.base.removeKey(key);
		}
	}

	//! Indirect object
	public boolean IsIndirect() {
		return this.base != null && this.base.isIndirect();
	}

	public COSKey getKey() {
		final COSKey key = new COSKey();
		return this.base != null ? this.base.getKey() : key;
	}

	public COSDocument getDocument() {
		return this.base != null ? this.base.getDocument() : null;
	}

	public void setKey(final COSKey key, final COSDocument document) {
		if (this.base == null || this.base.setKey(key, document)) {
			this.base = new COSIndirect(key, document);
		}
	}

	//	const COSObject GetDirect() const;
	public COSObject getDirect() {
		return this.base != null ? this.base.getDirect() : new COSObject();
	}

	public void setDirect(final COSObject value) {
		if (this.base == null || !this.base.setDirect(value)) {
			set(value.base);
		}
	}

	//! Marks object for incremental update.
	//! (If object is indirect and its document is known.)
	public void mark() {
		if (this.base != null) {
			this.base.mark();
		}
	}

	public static COSObject getEmpty() {
		return empty;
	}

}
