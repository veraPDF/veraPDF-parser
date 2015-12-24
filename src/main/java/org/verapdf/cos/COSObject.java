package org.verapdf.cos;

import org.verapdf.as.ASAtom;

/**
 * @author Timur Kamalov
 */
public class COSObject {

	private COSBase base;

	public COSObject() {
	}

	public COSObject(final COSBase base) {
		this.base = base;
	}

	public COSObject(final COSObject object) {
		set(object.base);
	}

	public COSKey getKey() {
		final COSKey key = new COSKey();
		return this.base != null ? this.base.getKey() : key;
	}

	public COSObject getDirect() {
		return this.base != null ? this.base.getObject() : new COSObject();
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

	// OBJECT TYPE
	public COSObjType getType() {
		return this.base != null ? this.base.getType() : COSObjType.COSUndefinedT;
	}

	// INTEGER NUMBERS
	public long getInteger() {
		return this.base != null ? this.base.getInteger() : 0;
	}

	// NAMES
	public ASAtom getName() {
		final ASAtom empty = new ASAtom();
		return this.base != null ? this.base.getName() : empty;

	}

	// DICTIONARIES
	public COSObject getKey(final ASAtom key) {
		return this.base != null ? this.base.getKey(key) : new COSObject();
	}

	public void setKey(final ASAtom key, final COSObject value) {
		if (this.base == null || !this.base.setKey(key, value)) {
			// TODO : impossible assign value to this in java.
		}
	}

	// ARRAYS
	public void add(final COSObject value) {
		if (this.base == null || !this.base.add(value)) {
			// TODO : impossible assign value to this in java.
		}
	}

	public boolean empty() {
		return this.base == null;
	}

	public void clear() {
		this.set(null);
	}

}
